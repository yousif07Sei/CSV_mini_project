package com.ga.processor.concurrent_csv_processor.controller;

import com.ga.processor.concurrent_csv_processor.dto.request.ProcessingRequest;
import com.ga.processor.concurrent_csv_processor.dto.response.ProcessingResponse;
import com.ga.processor.concurrent_csv_processor.model.Employee;
import com.ga.processor.concurrent_csv_processor.service.CsvReaderService;
import com.ga.processor.concurrent_csv_processor.service.SalaryProcessorService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final CsvReaderService csvReaderService;
    private final SalaryProcessorService salaryProcessorService;

    public EmployeeController(CsvReaderService csvReaderService,
                              SalaryProcessorService salaryProcessorService) {
        this.csvReaderService = csvReaderService;
        this.salaryProcessorService = salaryProcessorService;
    }


    @GetMapping
    public ResponseEntity<List<Employee>> getEmployees() {
        try {
            List<Employee> employees = csvReaderService.readDefaultCsv();
            return ResponseEntity.ok(employees);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    @PostMapping("/process")
    public ResponseEntity<ProcessingResponse> processEmployees(
            @RequestBody(required = false) ProcessingRequest request) {
        if (request == null) request = new ProcessingRequest();
        try {
            List<Employee> employees = csvReaderService.readDefaultCsv();
            ProcessingResponse response = salaryProcessorService.processEmployees(employees, request);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    @PostMapping("/export")
    public ResponseEntity<byte[]> exportCsv(
            @RequestBody(required = false) ProcessingRequest request) {
        if (request == null) request = new ProcessingRequest();
        try {
            List<Employee> employees = csvReaderService.readDefaultCsv();
            ProcessingResponse response = salaryProcessorService.processEmployees(employees, request);

            StringBuilder csv = new StringBuilder();
            csv.append("id,name,role,currentSalary,newSalary,increasePercentage,yearsOfService,reason\n");
            for (Employee e : response.getEmployees()) {
                csv.append(e.getId()).append(",")
                        .append(e.getName()).append(",")
                        .append(e.getRole()).append(",")
                        .append(e.getCurrent_salary()).append(",")
                        .append(e.getNew_Salary()).append(",")
                        .append(e.getTotal_Increase_Percentage()).append(",")
                        .append(e.getYear_Of_Service()).append(",")
                        .append(e.getIncrease_Reason()).append("\n");
            }

            byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"processed_employees.csv\"")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(bytes);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
