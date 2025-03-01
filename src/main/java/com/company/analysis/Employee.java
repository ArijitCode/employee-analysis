// Employee.java
package com.company.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Employee {
    private final int id;
    private final String firstName;
    private final String lastName;
    private final double salary;
    private final Integer managerId;
    private Employee manager;
    private final List<Employee> directReports = new ArrayList<>();
    // Cache fields for performance optimization
    private Double averageSubordinateSalary;
    private Integer managerCount;

    public Employee(int id, String firstName, String lastName, double salary, Integer managerId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.salary = salary;
        this.managerId = managerId;
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public double getSalary() {
        return salary;
    }

    public Integer getManagerId() {
        return managerId;
    }

    public Employee getManager() {
        return manager;
    }

    public void setManager(Employee manager) {
        this.manager = manager;
    }

    public List<Employee> getDirectReports() {
        return directReports;
    }
    
    public void addDirectReport(Employee employee) {
        directReports.add(employee);
    }
    
    public double getAverageSubordinateSalary() {
        if (averageSubordinateSalary == null) {
            if (directReports.isEmpty()) {
                averageSubordinateSalary = 0.0;
            } else {
                double totalSalary = directReports.stream()
                    .mapToDouble(Employee::getSalary)
                    .sum();
                averageSubordinateSalary = totalSalary / directReports.size();
            }
        }
        return averageSubordinateSalary;
    }
    
    public int getManagerCount() {
        if (managerCount == null) {
            if (manager == null) {
                managerCount = 0;
            } else {
                managerCount = 1 + manager.getManagerCount();
            }
        }
        return managerCount;
    }
    
    public double getExpectedMinimumSalary() {
        double avgSubordinateSalary = getAverageSubordinateSalary();
        return avgSubordinateSalary * 1.2;
    }
    
    public double getExpectedMaximumSalary() {
        double avgSubordinateSalary = getAverageSubordinateSalary();
        return avgSubordinateSalary * 1.5;
    }
    
    public boolean isUnderpaid() {
        return !directReports.isEmpty() && salary < getExpectedMinimumSalary();
    }
    
    public boolean isOverpaid() {
        return !directReports.isEmpty() && salary > getExpectedMaximumSalary();
    }
    
    public boolean hasLongReportingLine() {
        return getManagerCount() > 4;
    }
    
    public double getSalaryDeficit() {
        return getExpectedMinimumSalary() - salary;
    }
    
    public double getSalaryExcess() {
        return salary - getExpectedMaximumSalary();
    }
    
    public int getExcessManagerCount() {
        return getManagerCount() - 4;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return id == employee.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", name='" + firstName + " " + lastName + '\'' +
                ", salary=" + salary +
                '}';
    }
}
