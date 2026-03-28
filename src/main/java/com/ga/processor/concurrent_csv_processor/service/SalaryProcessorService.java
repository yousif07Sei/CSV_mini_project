package com.ga.processor.concurrent_csv_processor.service;

import com.ga.processor.concurrent_csv_processor.dto.request.ProcessingRequest;
import com.ga.processor.concurrent_csv_processor.dto.response.ProcessingResponse;
import com.ga.processor.concurrent_csv_processor.model.Employee;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class SalaryProcessorService {


    private final ExecutorService executorService;


    private final Semaphore writeSemaphore = new Semaphore(4, true);


    private final ReentrantLock statsLock = new ReentrantLock(true);


    private final AtomicInteger processedCount = new AtomicInteger(0);


    private double totalSalaryBefore;
    private double totalSalaryAfter;
    private int raisedCount;
    private int skippedCount;


    public SalaryProcessorService(ExecutorService employeeProcessorExecutorService) {
        this.executorService = employeeProcessorExecutorService;
    }


    public ProcessingResponse processEmployees(List<Employee> employees, ProcessingRequest request) {

        resetState();
        processedCount.set(0);

        long startTime = System.currentTimeMillis();


        List<Employee> results = new CopyOnWriteArrayList<>();


        List<CompletableFuture<Void>> futures = employees.stream()
                .map(emp -> CompletableFuture
                        .supplyAsync(() -> calculateSalary(emp, request), executorService)
                        .thenAccept(processed -> {
                            results.add(processed);
                            updateStats(processed);
                            processedCount.incrementAndGet();
                        })
                )
                .toList();


        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        long elapsedMs = System.currentTimeMillis() - startTime;

        return buildResponse(results, elapsedMs);
    }


    private Employee calculateSalary(Employee employee, ProcessingRequest request) {
        Employee result = copyEmployee(employee);

        try {
            writeSemaphore.acquire();


            if (employee.getProject_Completion_Percentage() < request.getMinCompletionThreshold()) {
                result.setNew_Salary(employee.getCurrent_salary());
                result.setTotal_Increase_Percentage(0.0);
                result.setIncrease_Reason("No raise – completion below " + request.getMinCompletionThreshold() + "%");
                return result;
            }


            double roleIncrease = switch (employee.getRole().trim().toUpperCase()) {
                case "DIRECTOR" -> request.getDirectorIncrease();
                case "MANAGER"  -> request.getManagerIncrease();
                default         -> request.getEmployeeIncrease();
            };


            int yearsWorked = Period.between(employee.getJoined_Date(), LocalDate.now()).getYears();
            double serviceIncrease = yearsWorked * request.getYearlyIncrease();

            double totalIncrease = roleIncrease + serviceIncrease;
            double newSalary = employee.getCurrent_salary() * (1.0 + totalIncrease / 100.0);

            result.setNew_Salary(Math.round(newSalary * 100.0) / 100.0);
            result.setTotal_Increase_Percentage(Math.round(totalIncrease * 100.0) / 100.0);
            result.setYear_Of_Service(yearsWorked);
            result.setIncrease_Reason("Role (" + employee.getRole() + "): +" + roleIncrease + "%" +
                    (yearsWorked > 0 ? ", Service (" + yearsWorked + " yrs): +" + serviceIncrease + "%" : "") +
                    " → Total: +" + Math.round(totalIncrease * 100.0) / 100.0 + "%");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            result.setNew_Salary(employee.getCurrent_salary());
            result.setIncrease_Reason("Processing interrupted");
        } finally {
            writeSemaphore.release();
        }

        return result;
    }


    private void updateStats(Employee emp) {
        statsLock.lock();
        try {
            totalSalaryBefore += emp.getCurrent_salary();
            totalSalaryAfter  += emp.getNew_Salary();
            if (emp.getTotal_Increase_Percentage() > 0) {
                raisedCount++;
            } else {
                skippedCount++;
            }
        } finally {
            statsLock.unlock();
        }
    }


    private void resetState() {
        statsLock.lock();
        try {
            totalSalaryBefore = 0;
            totalSalaryAfter  = 0;
            raisedCount       = 0;
            skippedCount      = 0;
        } finally {
            statsLock.unlock();
        }
    }


    private ProcessingResponse buildResponse(List<Employee> employees, long elapsedMs) {
        return ProcessingResponse.builder()
                .employees(employees)
                .totalEmployees(employees.size())
                .raisedEmployees(raisedCount)
                .skippedEmployees(skippedCount)
                .totalSalaryBefore(Math.round(totalSalaryBefore * 100.0) / 100.0)
                .totalSalaryAfter(Math.round(totalSalaryAfter * 100.0) / 100.0)
                .totalSalaryIncrease(Math.round((totalSalaryAfter - totalSalaryBefore) * 100.0) / 100.0)
                .processingTimeMs(elapsedMs)
                .threadPoolSize(8)
                .build();
    }


    private Employee copyEmployee(Employee src) {
        return Employee.builder()
                .id(src.getId())
                .name(src.getName())
                .current_salary(src.getCurrent_salary())
                .joined_Date(src.getJoined_Date())
                .role(src.getRole())
                .project_Completion_Percentage(src.getProject_Completion_Percentage())
                .build();
    }
}
