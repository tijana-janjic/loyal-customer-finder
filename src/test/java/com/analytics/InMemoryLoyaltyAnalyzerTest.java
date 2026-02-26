package com.analytics;

import com.analytics.service.InMemoryLoyaltyAnalyzer;
import com.analytics.service.LoyaltyAnalyzer;

class InMemoryLoyaltyAnalyzerTest extends LoyaltyAnalyzerContractTest {

    @Override
    LoyaltyAnalyzer analyzer() {
        return new InMemoryLoyaltyAnalyzer();
    }
}
