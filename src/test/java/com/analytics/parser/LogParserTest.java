package com.analytics.parser;

import com.analytics.model.LogRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LogParserTest {

    @TempDir
    Path tempDir;

    private final LogParser parser = new LogParser();

    private Path write(String filename, String... lines) throws IOException {
        Path file = tempDir.resolve(filename);
        Files.writeString(file, String.join("\n", lines));
        return file;
    }

    @Test
    void parsesWellFormedLines() throws IOException {
        Path file = write("log.txt",
                "2024-01-15T10:00:00 /home cust_001",
                "2024-01-15T10:05:00 /about cust_002");

        List<LogRecord> records = parser.parseFile(file);

        assertThat(records).hasSize(2);
        assertThat(records.get(0)).isEqualTo(new LogRecord("2024-01-15T10:00:00", "/home", "cust_001"));
        assertThat(records.get(1)).isEqualTo(new LogRecord("2024-01-15T10:05:00", "/about", "cust_002"));
    }

    @Test
    void skipsBlankLines() throws IOException {
        Path file = write("log.txt",
                "",
                "2024-01-15T10:00:00 /home cust_001",
                "");

        assertThat(parser.parseFile(file)).hasSize(1);
    }

    @Test
    void skipsMalformedLines() throws IOException {
        Path file = write("log.txt",
                "BADLINE",
                "2024-01-15T10:00:00 /home cust_001");

        List<LogRecord> records = parser.parseFile(file);

        assertThat(records).hasSize(1);
        assertThat(records.get(0).customerId()).isEqualTo("cust_001");
    }

    @Test
    void emptyFileReturnsEmptyList() throws IOException {
        Path file = write("log.txt");

        assertThat(parser.parseFile(file)).isEmpty();
    }

    @Test
    void handlesExtraWhitespace() throws IOException {
        Path file = write("log.txt",
                "2024-01-15T10:00:00   /home   cust_001");

        List<LogRecord> records = parser.parseFile(file);

        assertThat(records).hasSize(1);
        assertThat(records.get(0)).isEqualTo(new LogRecord("2024-01-15T10:00:00", "/home", "cust_001"));
    }
}
