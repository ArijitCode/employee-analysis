// EmployeeAnalysisService.java
package com.company.analysis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

public class EmployeeAnalysisService {
    private static final int BATCH_SIZE = 10000; // Process in batches for better performance
    private final ForkJoinPool threadPool;
    
    public EmployeeAnalysisService(ForkJoinPool threadPool) {
        this.threadPool = threadPool;
    }
    
    public List<String> analyzeFromCsv(Path csvPath) throws IOException {
        List<String> lines = Files.readAllLines(csvPath);
        
        // Skip the header
        if (lines.size() > 0) {
            lines = lines.subList(1, lines.size());
        }
        
        // Parse employees
        Map<Integer, Employee> employeesById = parseEmployees(lines);
        
        // Build organizational hierarchy
        buildOrgHierarchy(employeesById);
        
        // Analyze and generate reports
        return generateReports(employeesById);
    }
    
    private Map<Integer, Employee> parseEmployees(List<String> lines) {
        return threadPool.invoke(new ParseEmployeesTask(lines, 0, lines.size()));
    }
    
    private static class ParseEmployeesTask extends RecursiveTask<Map<Integer, Employee>> {
        private final List<String> lines;
        private final int start;
        private final int end;
        
        public ParseEmployeesTask(List<String> lines, int start, int end) {
            this.lines = lines;
            this.start = start;
            this.end = end;
        }
        
        @Override
        protected Map<Integer, Employee> compute() {
            if (end - start <= BATCH_SIZE) {
                Map<Integer, Employee> result = new HashMap<>();
                for (int i = start; i < end; i++) {
                    try {
                        String line = lines.get(i);
                        String[] parts = line.split(",");
                        
                        int id = Integer.parseInt(parts[0]);
                        String firstName = parts[1];
                        String lastName = parts[2];
                        double salary = Double.parseDouble(parts[3]);
                        Integer managerId = parts.length > 4 && !parts[4].isEmpty() ? 
                                           Integer.parseInt(parts[4]) : null;
                        
                        Employee employee = new Employee(id, firstName, lastName, salary, managerId);
                        result.put(id, employee);
                    } catch (Exception e) {
                        // Skip invalid lines but log the error
                        System.err.println("Error parsing line: " + lines.get(i) + " - " + e.getMessage());
                    }
                }
                return result;
            } else {
                int mid = start + (end - start) / 2;
                ParseEmployeesTask leftTask = new ParseEmployeesTask(lines, start, mid);
                ParseEmployeesTask rightTask = new ParseEmployeesTask(lines, mid, end);
                
                leftTask.fork();
                Map<Integer, Employee> rightResult = rightTask.compute();
                Map<Integer, Employee> leftResult = leftTask.join();
                
                Map<Integer, Employee> result = new HashMap<>(leftResult);
                result.putAll(rightResult);
                return result;
            }
        }
    }
    
    private void buildOrgHierarchy(Map<Integer, Employee> employeesById) {
        // Using ConcurrentHashMap to safely parallelize
        Map<Integer, List<Employee>> employeesByManager = new ConcurrentHashMap<>();
        
        // Group employees by manager
        employeesById.values().parallelStream().forEach(employee -> {
            Integer managerId = employee.getManagerId();
            if (managerId != null) {
                employeesByManager.computeIfAbsent(managerId, k -> new ArrayList<>())
                                  .add(employee);
            }
        });
        
        // Link managers with their direct reports
        employeesByManager.forEach((managerId, directReports) -> {
            Employee manager = employeesById.get(managerId);
            if (manager != null) {
                directReports.forEach(employee -> {
                    employee.setManager(manager);
                    manager.addDirectReport(employee);
                });
            }
        });
    }
    
    private List<String> generateReports(Map<Integer, Employee> employeesById) {
        List<String> reports = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("#,##0.00");
        
        reports.add("ORGANIZATIONAL ANALYSIS REPORT");
        reports.add("===========================");
        reports.add("");
        
        // Underpaid managers
        List<Employee> underpaidManagers = employeesById.values().parallelStream()
            .filter(Employee::isUnderpaid)
            .collect(Collectors.toList());
        
        reports.add("1. UNDERPAID MANAGERS");
        reports.add("-------------------");
        if (underpaidManagers.isEmpty()) {
            reports.add("No underpaid managers found.");
        } else {
            for (Employee manager : underpaidManagers) {
                reports.add(String.format("%s %s (ID: %d) is underpaid by $%s. " +
                    "Current salary: $%s, required minimum: $%s.",
                    manager.getFirstName(), manager.getLastName(), manager.getId(),
                    df.format(manager.getSalaryDeficit()),
                    df.format(manager.getSalary()),
                    df.format(manager.getExpectedMinimumSalary())));
            }
        }
        reports.add("");
        
        // Overpaid managers
        List<Employee> overpaidManagers = employeesById.values().parallelStream()
            .filter(Employee::isOverpaid)
            .collect(Collectors.toList());
        
        reports.add("2. OVERPAID MANAGERS");
        reports.add("------------------");
        if (overpaidManagers.isEmpty()) {
            reports.add("No overpaid managers found.");
        } else {
            for (Employee manager : overpaidManagers) {
                reports.add(String.format("%s %s (ID: %d) is overpaid by $%s. " +
                    "Current salary: $%s, maximum allowed: $%s.",
                    manager.getFirstName(), manager.getLastName(), manager.getId(),
                    df.format(manager.getSalaryExcess()),
                    df.format(manager.getSalary()),
                    df.format(manager.getExpectedMaximumSalary())));
            }
        }
        reports.add("");
        
        // Long reporting lines
        List<Employee> longReportingLines = employeesById.values().parallelStream()
            .filter(Employee::hasLongReportingLine)
            .collect(Collectors.toList());
        
        reports.add("3. EMPLOYEES WITH LONG REPORTING LINES");
        reports.add("-------------------------------------");
        if (longReportingLines.isEmpty()) {
            reports.add("No employees with excessively long reporting lines found.");
        } else {
            for (Employee employee : longReportingLines) {
                reports.add(String.format("%s %s (ID: %d) has a reporting line that is too long by %d managers. " +
                    "Current: %d managers in chain.",
                    employee.getFirstName(), employee.getLastName(), employee.getId(),
                    employee.getExcessManagerCount(),
                    employee.getManagerCount()));
            }
        }
        
        reports.add("");
        reports.add("END OF REPORT");
        
        return reports;
    }
}
