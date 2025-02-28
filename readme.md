# Employee Organization Analysis

This Java application analyzes employee organizational data to identify potential improvements in the company structure. It processes CSV files containing employee information to report on:

1. Managers who earn less than the required minimum (20% more than the average salary of their direct subordinates)
2. Managers who earn more than the allowed maximum (50% more than the average salary of their direct subordinates)
3. Employees who have excessively long reporting lines (more than 4 managers between them and the CEO)

## Requirements

- Java 11 or higher
- Maven 3.6 or higher

## Building the Application

To build the application, run:

```bash
mvn clean install
```

This will create an executable JAR file `employee-analysis-1.0-SNAPSHOT-jar-with-dependencies.jar` in the `target` directory.

## Running the Application

To run the application:

```bash
java -jar target/employee-analysis-1.0-SNAPSHOT-jar-with-dependencies.jar path/to/employees.csv
```

## CSV File Format

The input CSV file should have the following structure:

```
Id,firstName,lastName,salary,managerId
123,Joe,Doe,60000,
124,Martin,Chekov,45000,123
125,Bob,Ronstad,47000,123
```

Where:
- `Id` - Employee ID (integer)
- `firstName` - First name
- `lastName` - Last name
- `salary` - Annual salary (numeric)
- `managerId` - ID of the employee's manager (blank for CEO)

## Performance Considerations

The application is designed to handle large CSV files efficiently:

- Uses Java's Fork/Join framework for parallel processing
- Processes data in batches for better memory usage
- Thread-safe implementation for concurrent operations

## Assumptions

1. There is exactly one CEO (employee with no manager)
2. Employee IDs are unique
3. There are no circular references in the management chain
4. All salaries are positive numbers
5. The organizational hierarchy is a tree structure (each employee has at most one manager)

## Testing

Run the tests with:

```bash
mvn test
```
