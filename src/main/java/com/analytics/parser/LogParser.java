package com.analytics.parser;

import com.analytics.model.LogRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Parses a log file into a list of LogRecord objects.
 * Expected line format (whitespace-separated): timestamp pageId customerId
 * Blank lines are skipped. Malformed lines are logged as warnings and skipped,
 * so a single bad line never aborts the entire file.
 */
public class LogParser {

    private static final Logger LOGGER = Logger.getLogger(LogParser.class.getName());
    private static final int EXPECTED_FIELDS = 3;

    public List<LogRecord> parseFile(Path filePath) throws IOException {
        List<String> lines = Files.readAllLines(filePath);
        List<LogRecord> records = new ArrayList<>(lines.size());

        for (int lineNumber = 1; lineNumber <= lines.size(); lineNumber++) {
            String line = lines.get(lineNumber - 1).strip();
            if (line.isEmpty()) continue;
            parseFileLine(line, lineNumber, filePath).ifPresent(records::add);
        }

        return records;
    }

    public Optional<LogRecord> parseFileLine(String line, int lineNumber, Path filePath) {
        String[] parts = line.split("\\s+", EXPECTED_FIELDS);
        if (parts.length != EXPECTED_FIELDS) {
            LOGGER.warning("Malformed log entry at %s:%d: \"%s\""
                    .formatted(filePath.getFileName(), lineNumber, line));
            return Optional.empty();
        }
        return Optional.of(new LogRecord(parts[0], parts[1], parts[2]));
    }
}
