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
