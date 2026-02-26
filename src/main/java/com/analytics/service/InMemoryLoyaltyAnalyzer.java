package com.analytics.service;

import com.analytics.model.CustomerActivity;
import com.analytics.model.LogRecord;
import com.analytics.parser.LogParser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Loads both log files into memory and aggregates customer activity before evaluating loyalty. */
public class InMemoryLoyaltyAnalyzer implements LoyaltyAnalyzer {

    private final LogParser parser = new LogParser();

    @Override
    public List<String> find(Path dayX, Path dayY) throws IOException {
        Map<String, CustomerActivity> activities = new HashMap<>();

        for (LogRecord r : parser.parseFile(dayX))
            activities.computeIfAbsent(r.customerId(), CustomerActivity::new).recordDayX(r.pageId());

        for (LogRecord r : parser.parseFile(dayY))
            activities.computeIfAbsent(r.customerId(), CustomerActivity::new).recordDayY(r.pageId());

        List<String> loyal = new ArrayList<>();
        for (CustomerActivity a : activities.values()) {
            if (a.isLoyal()) {
                loyal.add(a.customerId());
            }
        }

        return loyal;
    }
}
