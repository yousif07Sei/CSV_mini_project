package com.ga.processor.concurrent_csv_processor;
import com.ga.processor.concurrent_csv_processor.dto.request.ProcessingRequest;
import com.ga.processor.concurrent_csv_processor.dto.response.ProcessingResponse;
import com.ga.processor.concurrent_csv_processor.model.Employee;
import com.ga.processor.concurrent_csv_processor.service.SalaryProcessorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executors;
import static org.junit.jupiter.api.Assertions.*;

public class SalaryProcessorServiceTest {

    private SalaryProcessorService service;
    private ProcessingRequest defaultRequest;

    @BeforeEach
    void setUp() {
        service = new SalaryProcessorService(Executors.newFixedThreadPool(4));
        defaultRequest = new ProcessingRequest();
    }

    @Test
    void testEmployeeBelowThresholdGetsNoRaise() {
        Employee emp = Employee.builder()
                .id(1L)
                .name("Test")
                .current_salary(50000)
                .joined_Date(LocalDate.now().minusYears(3))
                .role("Employee")
                .project_Completion_Percentage(45.0)
                .build();

        ProcessingResponse result = service.processEmployees(List.of(emp), defaultRequest);

        assertEquals(50000, result.getEmployees().get(0).getNew_Salary());
        assertEquals(0.0, result.getEmployees().get(0).getTotal_Increase_Percentage());
    }

    @Test
    void testDirectorGets5PercentRaise() {
        Employee emp = Employee.builder()
                .id(2L)
                .name("Charlie")
                .current_salary(75000)
                .joined_Date(LocalDate.now().minusMonths(6))
                .role("Director")
                .project_Completion_Percentage(70.0)
                .build();

        ProcessingResponse result = service.processEmployees(List.of(emp), defaultRequest);

        assertEquals(78750.0, result.getEmployees().get(0).getNew_Salary(), 0.01);
        assertEquals(5.0, result.getEmployees().get(0).getTotal_Increase_Percentage(), 0.01);
    }

    @Test
    void testManagerGets2PercentRaise() {
        Employee emp = Employee.builder()
                .id(3L)
                .name("Bob")
                .current_salary(68000)
                .joined_Date(LocalDate.now().minusMonths(6))
                .role("Manager")
                .project_Completion_Percentage(60.0)
                .build();

        ProcessingResponse result = service.processEmployees(List.of(emp), defaultRequest);

        assertEquals(69360.0, result.getEmployees().get(0).getNew_Salary(), 0.01);
        assertEquals(2.0, result.getEmployees().get(0).getTotal_Increase_Percentage(), 0.01);
    }

    @Test
    void testEmployeeGets1PercentRaise() {
        Employee emp = Employee.builder()
                .id(4L)
                .name("Alice")
                .current_salary(52000)
                .joined_Date(LocalDate.now().minusMonths(6))
                .role("Employee")
                .project_Completion_Percentage(80.0)
                .build();

        ProcessingResponse result = service.processEmployees(List.of(emp), defaultRequest);

        assertEquals(52520.0, result.getEmployees().get(0).getNew_Salary(), 0.01);
        assertEquals(1.0, result.getEmployees().get(0).getTotal_Increase_Percentage(), 0.01);
    }

    @Test
    void testYearsOfServiceIncrement() {
        Employee emp = Employee.builder()
                .id(5L)
                .name("Nate")
                .current_salary(100000)
                .joined_Date(LocalDate.now().minusYears(3))
                .role("Employee")
                .project_Completion_Percentage(70.0)
                .build();

        ProcessingResponse result = service.processEmployees(List.of(emp), defaultRequest);

        // 1% role + 3 years x 2% = 7% total
        assertEquals(107000.0, result.getEmployees().get(0).getNew_Salary(), 0.01);
        assertEquals(3, result.getEmployees().get(0).getYear_Of_Service());
    }

    @Test
    void testConcurrentProcessingReturnsCorrectCount() {
        List<Employee> employees = java.util.stream.IntStream.range(0, 30)
                .mapToObj(i -> Employee.builder()
                        .id((long) i)
                        .name("Employee " + i)
                        .current_salary(50000)
                        .joined_Date(LocalDate.now().minusYears(i % 5))
                        .role(i % 3 == 0 ? "Director" : i % 3 == 1 ? "Manager" : "Employee")
                        .project_Completion_Percentage(60 + i)
                        .build())
                .toList();

        ProcessingResponse result = service.processEmployees(employees, defaultRequest);

        assertEquals(30, result.getTotalEmployees());
        assertEquals(30, result.getEmployees().size());
        assertEquals(result.getRaisedEmployees() + result.getSkippedEmployees(),
                result.getTotalEmployees());
    }

}
