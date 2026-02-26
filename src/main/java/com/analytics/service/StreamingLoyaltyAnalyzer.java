package com.analytics.service;

import com.analytics.model.LogRecord;
import com.analytics.parser.LogParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Alternative implementation included for reference only.
 * Identifies loyal customers from two daily log files.
 * A customer is loyal if they appear on both days and visit at least 2 unique pages in total.
 * The algorithm runs in two phases:
 *   1. Parse day X — build two candidate collections:
 *        multiPage  (Set) — customers with 2+ unique pages; loyal on any day Y appearance
 *        singlePage (Map) — customers with exactly 1 unique page, mapped to that page;
 *                           loyal only if they visit a different page on day Y
 *   2. Stream day Y — match entries against the candidate collections, collecting loyal IDs.
 *      Stops early once both collections are empty.
 * Neither file is fully loaded into memory; both are streamed line by line.
 * Expected log format (whitespace-separated): timestamp pageId customerId
 */
public class StreamingLoyaltyAnalyzer implements LoyaltyAnalyzer {

    private final LogParser parser = new LogParser();

    @Override
    public List<String> find(Path dayX, Path dayY) throws IOException {
        Map<String, String> singlePage = new HashMap<>();
        Set<String> multiPage = new HashSet<>();

        classifyDayXCustomers(dayX, singlePage, multiPage);

        List<String> loyal = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(dayY)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null && !(multiPage.isEmpty() && singlePage.isEmpty())) {
                lineNumber++;
                String trimmed = line.strip();
                if (trimmed.isEmpty()) continue;

                Optional<LogRecord> entry = parser.parseFileLine(trimmed, lineNumber, dayY);
                if (entry.isEmpty()) continue;

                String pageId = entry.get().pageId();
                String customerId = entry.get().customerId();

                if (multiPage.remove(customerId)) {
                    loyal.add(customerId);
                } else {
                    String onlyPage = singlePage.get(customerId);
                    if (onlyPage != null && !onlyPage.equals(pageId)) {
                        singlePage.remove(customerId);
                        loyal.add(customerId);
                    }
                }
            }
        }

        return loyal;
    }

    /**
     * Reads day-X logs and populates the two candidate collections.
     * Customers promoted to multiPage are removed from singlePage.
     */
    private void classifyDayXCustomers(Path file, Map<String, String> singlePage, Set<String> multiPage)
            throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.strip();
                if (trimmed.isEmpty()) continue;

                Optional<LogRecord> entry = parser.parseFileLine(trimmed, lineNumber, file);
                if (entry.isEmpty()) continue;

                String pageId = entry.get().pageId();
                String customerId = entry.get().customerId();

                if (multiPage.contains(customerId)) continue;

                String prev = singlePage.get(customerId);
                if (prev == null) {
                    singlePage.put(customerId, pageId);
                } else if (!prev.equals(pageId)) {
                    singlePage.remove(customerId);
                    multiPage.add(customerId);
                }
            }
        }
    }
}
