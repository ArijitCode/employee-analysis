// EmployeeAnalysisServiceTest.java
package com.company.analysis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeAnalysisServiceTest {
    
    private EmployeeAnalysisService service;
    
    @BeforeEach
    void setUp() {
        service = new EmployeeAnalysisService(new ForkJoinPool(2));
    }
    
    @Test
    void testBasicStructure(@TempDir Path tempDir) throws IOException {
        // Create a test CSV file
        Path csvPath = tempDir.resolve("employees.csv");
        Files.writeString(csvPath, 
            "Id,firstName,lastName,salary,managerId\n" +
            "123,Joe,Doe,60000,\n" +
            "124,Martin,Chekov,45000,123\n" +
            "125,Bob,Ronstad,47000,123\n" +
            "300,Alice,Hasacat,50000,124\n" +
            "305,Brett,Hardleaf,34000,300\n"
        );
        
        List<String> report = service.analyzeFromCsv(csvPath);
        
        // Verify expected output format
        assertTrue(report.size() > 10, "Report should have multiple lines");
        assertTrue(report.get(0).contains("ORGANIZATIONAL ANALYSIS"), "Report should have correct header");
        
        // Check for specific content - CEO should be making appropriate salary
        boolean foundManager123 = false;
        for (String line : report) {
            if (line.contains("Joe Doe") && line.contains("123")) {
                foundManager123 = true;
                break;
            }
        }
        assertFalse(foundManager123, "CEO should be fairly compensated");
        
        // Check that Martin is underpaid (avg of Alice and Brett is 42000, 20% more is 50400)
        boolean foundUnderpaidManager = false;
        for (String line : report) {
            if (line.contains("Martin Chekov") && line.contains("underpaid")) {
                foundUnderpaidManager = true;
                break;
            }
        }
        assertTrue(foundUnderpaidManager, "Martin should be flagged as underpaid");
    }
    
    @Test
    void testLongReportingLine(@TempDir Path tempDir) throws IOException {
        // Create a test CSV with a long reporting chain
        Path csvPath = tempDir.resolve("long_chain.csv");
        Files.writeString(csvPath, 
            "Id,firstName,lastName,salary,managerId\n" +
            "100,CEO,Person,100000,\n" +
            "101,Level1,Manager,80000,100\n" +
            "102,Level2,Manager,70000,101\n" +
            "103,Level3,Manager,60000,102\n" +
            "104,Level4,Manager,50000,103\n" +
            "105,Employee,One,40000,104\n" +
            "106,Employee,Two,40000,105\n"
        );
        
        List<String> report = service.analyzeFromCsv(csvPath);
        
        // Employee Two should have a long reporting line (6 levels from CEO)
        boolean foundLongChain = false;
        for (String line : report) {
            if (line.contains("Employee Two") && line.contains("long")) {
                foundLongChain = true;
                break;
            }
        }
        assertTrue(foundLongChain, "Employee Two should be flagged for long reporting line");
    }
    
    @Test
    void testOverpaidManager(@TempDir Path tempDir) throws IOException {
        // Create a test CSV with an overpaid manager
        Path csvPath = tempDir.resolve("overpaid.csv");
        Files.writeString(csvPath, 
            "Id,firstName,lastName,salary,managerId\n" +
            "100,CEO,Person,100000,\n" +
            "101,Overpaid,Manager,60000,100\n" +
            "102,Staff,One,30000,101\n" +
            "103,Staff,Two,30000,101\n"
        );
        
        List<String> report = service.analyzeFromCsv(csvPath);
        
        // Overpaid Manager's average salary is 30000, max should be 45000
        boolean foundOverpaid = false;
        for (String line : report) {
            if (line.contains("Overpaid Manager") && line.contains("overpaid")) {
                foundOverpaid = true;
                break;
            }
        }
        assertTrue(foundOverpaid, "Overpaid Manager should be flagged");
    }
    
    @Test
    void testEmptyFile(@TempDir Path tempDir) throws IOException {
        // Test with just a header
        Path csvPath = tempDir.resolve("empty.csv");
        Files.writeString(csvPath, "Id,firstName,lastName,salary,managerId\n");
        
        List<String> report = service.analyzeFromCsv(csvPath);
        assertTrue(report.size() > 5, "Report should still generate with empty file");
    }
    
    @Test
    void testInvalidData(@TempDir Path tempDir) throws IOException {
        // Test with some invalid lines
        Path csvPath = tempDir.resolve("invalid.csv");
        Files.writeString(csvPath, 
            "Id,firstName,lastName,salary,managerId\n" +
            "123,Joe,Doe,60000,\n" +
            "invalid-line\n" +
            "124,Martin,Chekov,45000,123\n"
        );
        
        List<String> report = service.analyzeFromCsv(csvPath);
        assertTrue(report.size() > 5, "Report should still generate with invalid data");
    }
}
