package com.analytics;

import com.analytics.service.InMemoryLoyaltyAnalyzer;
import com.analytics.service.LoyaltyAnalyzer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class LoyaltyAnalyzerIntegrationTest {

    @Test
    void findsLoyalCustomersFromRealLogFiles() throws IOException, URISyntaxException {
        Path dayX = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("day_x.log")).toURI());
        Path dayY = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("day_y.log")).toURI());

        LoyaltyAnalyzer analyzer = new InMemoryLoyaltyAnalyzer();
        List<String> result = analyzer.find(dayX, dayY);

        // cust_001: visited both days, 2 unique pages (/home, /about) → loyal
        // cust_002: visited both days, 2 unique pages (/home, /contact) → loyal
        // cust_003: only day X → not loyal
        // cust_004: only day Y → not loyal
        assertThat(result).containsExactlyInAnyOrder("cust_001", "cust_002");
    }
}
