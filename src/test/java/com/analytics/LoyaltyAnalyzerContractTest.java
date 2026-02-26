package com.analytics;

import com.analytics.service.LoyaltyAnalyzer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contract tests for LoyaltyAnalyzer implementations.
 * Subclasses supply the implementation under test via analyzer().
 */
abstract class LoyaltyAnalyzerContractTest {

    @TempDir
    protected Path tempDir;

    abstract LoyaltyAnalyzer analyzer();

    protected Path writeLog(String filename, String... lines) throws IOException {
        Path file = tempDir.resolve(filename);
        Files.writeString(file, String.join("\n", lines));
        return file;
    }

    private List<String> find(Path dayX, Path dayY) throws IOException {
        return analyzer().find(dayX, dayY);
    }

    // --- core loyalty criteria ---

    @Test
    void customerVisitingBothDaysWithTwoPagesIsLoyal() throws IOException {
        Path x = writeLog("x.log",
                "2024-01-15T10:00:00 /home cust_001",
                "2024-01-15T10:05:00 /about cust_001");
        Path y = writeLog("y.log",
                "2024-01-16T09:00:00 /home cust_001");

        assertThat(find(x, y)).containsExactly("cust_001");
    }

    @Test
    void customerOnlyOnDayXIsNotLoyal() throws IOException {
        Path x = writeLog("x.log",
                "2024-01-15T10:00:00 /home cust_001",
                "2024-01-15T10:05:00 /about cust_001");
        Path y = writeLog("y.log",
                "2024-01-16T09:00:00 /home cust_002",
                "2024-01-16T09:05:00 /about cust_002");

        assertThat(find(x, y)).isEmpty();
    }

    @Test
    void customerOnlyOnDayYIsNotLoyal() throws IOException {
        Path x = writeLog("x.log",
                "2024-01-15T10:00:00 /home cust_002");
        Path y = writeLog("y.log",
                "2024-01-16T09:00:00 /home cust_001",
                "2024-01-16T09:05:00 /about cust_001");

        assertThat(find(x, y)).isEmpty();
    }

    @Test
    void customerVisitingBothDaysButOnlyOneUniquePageIsNotLoyal() throws IOException {
        Path x = writeLog("x.log",
                "2024-01-15T10:00:00 /home cust_001");
        Path y = writeLog("y.log",
                "2024-01-16T09:00:00 /home cust_001");

        assertThat(find(x, y)).isEmpty();
    }

    @Test
    void twoDistinctPagesAcrossDaysCountTowardMinimum() throws IOException {
        Path x = writeLog("x.log",
                "2024-01-15T10:00:00 /home cust_001");
        Path y = writeLog("y.log",
                "2024-01-16T09:00:00 /about cust_001");

        assertThat(find(x, y)).containsExactly("cust_001");
    }

    @Test
    void repeatedVisitsToSamePageCountAsOne() throws IOException {
        Path x = writeLog("x.log",
                "2024-01-15T10:00:00 /home cust_001",
                "2024-01-15T11:00:00 /home cust_001",
                "2024-01-15T12:00:00 /home cust_001");
        Path y = writeLog("y.log",
                "2024-01-16T09:00:00 /home cust_001");

        assertThat(find(x, y)).isEmpty();
    }

    // --- mixed scenarios ---

    @Test
    void mixedCustomersReturnOnlyLoyalOnes() throws IOException {
        Path x = writeLog("x.log",
                "2024-01-15T10:00:00 /home    cust_001",   // loyal
                "2024-01-15T10:05:00 /about   cust_001",
                "2024-01-15T10:10:00 /home    cust_002",   // both days, 2nd unique page on day Y → loyal
                "2024-01-15T10:15:00 /home    cust_003",   // only day X
                "2024-01-15T10:20:00 /products cust_003",
                "2024-01-15T10:25:00 /home    cust_005");  // both days, 1 unique page
        Path y = writeLog("y.log",
                "2024-01-16T09:00:00 /home    cust_001",
                "2024-01-16T09:05:00 /contact cust_002",
                "2024-01-16T09:15:00 /products cust_004",  // only day Y
                "2024-01-16T09:20:00 /about   cust_004",
                "2024-01-16T09:25:00 /home    cust_005");

        assertThat(find(x, y)).containsExactlyInAnyOrder("cust_001", "cust_002");
    }

    @Test
    void emptyFilesReturnEmptyList() throws IOException {
        Path x = writeLog("x.log");
        Path y = writeLog("y.log");

        assertThat(find(x, y)).isEmpty();
    }

    // --- robustness ---

    @Test
    void blankLinesAreIgnored() throws IOException {
        Path x = writeLog("x.log",
                "",
                "2024-01-15T10:00:00 /home cust_001",
                "",
                "2024-01-15T10:05:00 /about cust_001",
                "");
        Path y = writeLog("y.log",
                "2024-01-16T09:00:00 /contact cust_001");

        assertThat(find(x, y)).containsExactly("cust_001");
    }

    @Test
    void malformedLineIsSkipped() throws IOException {
        Path x = writeLog("x.log",
                "BADLINE",
                "2024-01-15T10:00:00 /home cust_001",
                "2024-01-15T10:05:00 /about cust_001");
        Path y = writeLog("y.log",
                "2024-01-16T09:00:00 /home cust_001");

        assertThat(find(x, y)).containsExactly("cust_001");
    }

    @Test
    void largeVolumeOfEntriesHandledCorrectly() throws IOException {
        StringBuilder xLines = new StringBuilder();
        StringBuilder yLines = new StringBuilder();

        for (int i = 0; i < 10_000; i++) {
            xLines.append("2024-01-15T10:00:00 /page-%d cust_%04d\n".formatted(i, i));
            yLines.append("2024-01-16T09:00:00 /page-%d cust_%04d\n".formatted(i, i));
        }
        // multiPage candidate: 2 pages on day X, appears on day Y
        xLines.append("2024-01-15T10:00:00 /home cust_MULTI\n");
        xLines.append("2024-01-15T10:01:00 /about cust_MULTI\n");
        yLines.append("2024-01-16T09:00:00 /home cust_MULTI\n");
        // singlePage candidate: 1 page on day X, different page on day Y
        xLines.append("2024-01-15T10:00:00 /home cust_SINGLE\n");
        yLines.append("2024-01-16T09:00:00 /about cust_SINGLE\n");

        Path x = tempDir.resolve("x.log");
        Path y = tempDir.resolve("y.log");
        Files.writeString(x, xLines.toString());
        Files.writeString(y, yLines.toString());

        assertThat(find(x, y)).containsExactlyInAnyOrder("cust_MULTI", "cust_SINGLE");
    }
}
