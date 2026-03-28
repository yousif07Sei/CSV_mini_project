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
}
