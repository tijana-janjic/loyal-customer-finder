package com.analytics.model;

/** Immutable representation of a single parsed log entry. */
public record LogRecord(String timestamp, String pageId, String customerId) {}
