package com.ga.processor.concurrent_csv_processor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder

public class Employee {

    private long id ;

    private String name;


    private double current_salary;

    private String role;

    private double project_Completion_percentage;

    private double new_Salary;

    private double total_Increase_percentage;

    private double year_Of_Service;

    private String increase_reason;



}
