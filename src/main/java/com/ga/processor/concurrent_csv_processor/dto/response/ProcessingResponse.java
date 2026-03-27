package com.ga.processor.concurrent_csv_processor.dto.response;

import com.ga.processor.concurrent_csv_processor.model.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingResponse {

    private List<Employee> employees;
    private int totalEmployees;
    private int raisedEmployees;
    private int skippedEmployees;
    private double totalSalaryBefore;
    private double totalSalaryAfter;
    private double totalSalaryIncrease;
    private long processingTimeMs;
    private int threadPoolSize;

}


