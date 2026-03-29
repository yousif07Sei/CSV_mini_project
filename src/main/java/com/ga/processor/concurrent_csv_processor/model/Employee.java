package com.ga.processor.concurrent_csv_processor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Represents an employee record read from the CSV file.
 * Contains both input fields from the CSV and output fields
 * calculated during salary processing.
 */

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder

public class Employee {

    private long id ;

    private String name;


    private double current_salary;

    private LocalDate joined_Date;

    private Role role;

    private double project_Completion_Percentage;

    private double new_Salary;

    private double total_Increase_Percentage;

    private int year_Of_Service;

    private String increase_Reason;



}
