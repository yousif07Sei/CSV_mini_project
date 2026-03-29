# Concurrent CSV Data Processor

A Spring Boot application that reads employee data from a CSV file and concurrently calculates salary increments using Java's concurrency primitives.

## Project Setup

### Prerequisites
- Java 17
- Maven 3.8+

### Steps to Run
1. Clone the repository
2. Open the project in IntelliJ
3. Run `mvn clean install`
4. Run the application from `ConcurrentCsvProcessorApplication.java`
5. The app starts on `http://localhost:8080`

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/employees | Returns raw employee list |
| POST | /api/employees/process | Processes salaries with given parameters |
| POST | /api/employees/export | Downloads processed results as CSV |

## Salary Rules

| Rule | Description |
|------|-------------|
| Completion < 60% | No raise |
| Director | +5% base increment |
| Manager | +2% base increment |
| Employee | +1% base increment |
| Years of Service | +2% per fully completed year |

## Concurrency Mechanisms

| Mechanism | Purpose |
|-----------|---------|
| ExecutorService | Thread pool to process employees in parallel |
| CompletableFuture | Submit tasks asynchronously |
| Semaphore | Limit concurrent write-backs |
| ReentrantLock | Protect shared counters |
| AtomicInteger | Lock-free progress tracking |
| CopyOnWriteArrayList | Thread-safe result collection |

## CSV File Format
```
id,name,currentSalary,joinedDate,role,projectCompletionPercentage
1,Alice,52000.0,2019-05-12,Employee,0.8
```

## Project Structure
```
src/main/java/com/ga/processor/concurrent_csv_processor/
├── config/
│   └── ThreadPoolConfig.java
├── controller/
│   └── EmployeeController.java
├── dto/
│   ├── request/
│   │   └── ProcessingRequest.java
│   └── response/
│       └── ProcessingResponse.java
├── model/
│   └── Employee.java
└── service/
    ├── CsvReaderService.java
    └── SalaryProcessorService.java

Project Report
==============

Overview
--------
The Concurrent CSV Data Processor is a Spring Boot application that reads employee 
data from a CSV file and concurrently calculates salary increments based on role, 
years of service, and project completion percentage.

Implementation Details
----------------------

CSV Reading
-----------
Employee data is read from `test_employees.csv` using OpenCSV library. 
A `ReentrantLock` ensures that only one thread can read the file at a time, 
preventing data corruption during concurrent access.

Concurrency Implementation
--------------------------
The application uses multiple concurrency mechanisms working together:
- `ExecutorService` with a fixed thread pool of 8 threads processes all employees in parallel
- `CompletableFuture.supplyAsync()` submits one task per employee asynchronously
- `Semaphore` limits the number of threads that can write results simultaneously to 4
- `ReentrantLock` protects shared counters like raised and skipped employee counts
- `AtomicInteger` tracks processed employee count without locking
- `CopyOnWriteArrayList` safely collects results from multiple threads

Salary Rules Applied
--------------------
- Employees with project completion below 60% receive no raise
- Directors receive a 5% base increment
- Managers receive a 2% base increment
- Employees receive a 1% base increment
- An additional 2% is added per fully completed year of service

Outcomes
--------
- All 30 employees were processed concurrently in under 5ms
- 21 employees received a raise
- 9 employees were skipped due to low project completion
- Total payroll increased from $2,035,000 to $2,240,900
- Total salary increase of $205,900
