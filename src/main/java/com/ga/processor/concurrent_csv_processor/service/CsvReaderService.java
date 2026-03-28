package com.ga.processor.concurrent_csv_processor.service;

import com.ga.processor.concurrent_csv_processor.model.Employee;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class CsvReaderService {

    // ReentrantLock to ensure only one thread reads the file at a time
    private final ReentrantLock fileLock = new ReentrantLock(true);

    // Reads the default employees.csv from src/main/resources
    public List<Employee> readDefaultCsv() throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("test_employees.csv");
        if (is == null) {
            throw new FileNotFoundException("employees.csv not found");
        }
        return parseStream(is);
    }

    // Reads an uploaded CSV file
    public List<Employee> readUploadedCsv(MultipartFile file) throws IOException {
        return parseStream(file.getInputStream());
    }

    // Parses the CSV stream into a list of Employee objects
    private List<Employee> parseStream(InputStream inputStream) throws IOException {
        fileLock.lock();
        try (
                InputStreamReader isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr);
                CSVReader csvReader = new CSVReader(br)
        ) {
            List<Employee> employees = new ArrayList<>();
            String[] line;

            while ((line = csvReader.readNext()) != null) {
                if (line.length < 6) continue;

                // Skip header row if first column is not a number
                try {
                    Integer.parseInt(line[0].trim());
                } catch (NumberFormatException e) {
                    continue;
                }

                Employee employee = Employee.builder()
                        .id(Long.parseLong(line[0].trim()))
                        .name(line[1].trim())
                        .current_salary(Double.parseDouble(line[2].trim()))
                        .joined_Date(LocalDate.parse(line[3].trim()))
                        .role(line[4].trim())
                        .project_Completion_Percentage(Double.parseDouble(line[5].trim()) * 100)
                        .build();

                employees.add(employee);
            }
            return employees;

        } catch (CsvValidationException e) {
            throw new IOException("CSV validation failed: " + e.getMessage(), e);
        } finally {
            fileLock.unlock();
        }
    }
}
