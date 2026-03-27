package com.ga.processor.concurrent_csv_processor.dto.request;

import lombok.Data;

@Data
public class ProcessingRequest {

    private double directorIncrease = 5.0;
    private double managerIncrease = 2.0;
    private double employeeIncrease = 1.0;
    private double minCompletionThreshold = 60.0;
    private double yearlyIncrease = 2.0;

}