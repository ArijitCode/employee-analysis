package com.company.analysis;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class App {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Please provide a CSV file path as an argument");
            System.exit(1);
        }

        String csvFilePath = args[0];
        Path path = Paths.get(csvFilePath);

        try {
            if (!Files.exists(path)) {
                throw new IllegalArgumentException("File does not exist: " + csvFilePath);
            }

            // Create service with thread count based on available processors
            int processors = Runtime.getRuntime().availableProcessors();
            EmployeeAnalysisService service = new EmployeeAnalysisService(
                new ForkJoinPool(processors)
            );

            // Parse CSV and analyze
            List<String> analysisResults = service.analyzeFromCsv(path);
            
            // Print results
            analysisResults.forEach(System.out::println);

        } catch (Exception e) {
            System.err.println("Error processing employee data: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
