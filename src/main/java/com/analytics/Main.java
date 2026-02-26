package com.analytics;

import com.analytics.service.InMemoryLoyaltyAnalyzer;
import com.analytics.service.LoyaltyAnalyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: loyalty-analyzer-in-memory <log_file_day_x> <log_file_day_y>");
            System.exit(1);
        }

        Path dayX = Path.of(args[0]);
        Path dayY = Path.of(args[1]);

        for (Path path : List.of(dayX, dayY)) {
            if (!Files.exists(path)) {
                System.err.println("Error: File not found: " + path);
                System.exit(1);
            }
        }

        try {
            LoyaltyAnalyzer finder = new InMemoryLoyaltyAnalyzer();
            List<String> loyalCustomers = finder.find(dayX, dayY);
            if (loyalCustomers.isEmpty()) {
                System.out.println("No loyal customers found.");
            } else {
                System.out.printf("Number of loyal customers found: %d.%n", loyalCustomers.size());
                for (String id : loyalCustomers) {
                    System.out.println("  " + id);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading log files: " + e.getMessage());
            System.exit(1);
        }
    }
}
