package com.analytics.model;

import java.util.HashSet;
import java.util.Set;

/** Tracks a single customer's activity across two days. */
public class CustomerActivity {

    private final String customerId;
    private final Set<String> pages;
    private boolean visitedDayX;
    private boolean visitedDayY;

    public CustomerActivity(String customerId) {
        this.customerId = customerId;
        this.pages = new HashSet<>();
    }

    public String customerId() {
        return customerId;
    }

    public boolean visitedDayX() {
        return visitedDayX;
    }

    public boolean visitedDayY() {
        return visitedDayY;
    }

    public int uniquePageCount() {
        return pages.size();
    }

    public void recordDayX(String pageId) {
        visitedDayX = true;
        pages.add(pageId);
    }

    public void recordDayY(String pageId) {
        visitedDayY = true;
        pages.add(pageId);
    }

    public boolean isLoyal() {
        return visitedDayX && visitedDayY && pages.size() >= 2;
    }
}
