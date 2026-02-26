package com.analytics.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerActivityTest {

    // --- recordDayX / recordDayY ---

    @Test
    void newActivityHasNoPagesAndHasNotVisitedEitherDay() {
        CustomerActivity a = new CustomerActivity("cust_001");

        assertThat(a.uniquePageCount()).isZero();
        assertThat(a.visitedDayX()).isFalse();
        assertThat(a.visitedDayY()).isFalse();
    }

    @Test
    void recordDayXMarksVisitAndAddsPage() {
        CustomerActivity a = new CustomerActivity("cust_001");
        a.recordDayX("/home");

        assertThat(a.visitedDayX()).isTrue();
        assertThat(a.visitedDayY()).isFalse();
        assertThat(a.uniquePageCount()).isEqualTo(1);
    }

    @Test
    void recordDayYMarksVisitAndAddsPage() {
        CustomerActivity a = new CustomerActivity("cust_001");
        a.recordDayY("/home");

        assertThat(a.visitedDayX()).isFalse();
        assertThat(a.visitedDayY()).isTrue();
        assertThat(a.uniquePageCount()).isEqualTo(1);
    }

    @Test
    void samePageRecordedMultipleTimesCountsOnce() {
        CustomerActivity a = new CustomerActivity("cust_001");
        a.recordDayX("/home");
        a.recordDayX("/home");
        a.recordDayY("/home");

        assertThat(a.uniquePageCount()).isEqualTo(1);
    }

    @Test
    void differentPagesAcrossDaysAreAllCounted() {
        CustomerActivity a = new CustomerActivity("cust_001");
        a.recordDayX("/home");
        a.recordDayY("/about");

        assertThat(a.uniquePageCount()).isEqualTo(2);
    }

    // --- isLoyal() ---

    @Test
    void loyalWhenVisitedBothDaysAndTwoUniquePages() {
        CustomerActivity a = new CustomerActivity("cust_001");
        a.recordDayX("/home");
        a.recordDayY("/about");

        assertThat(a.isLoyal()).isTrue();
    }

    @Test
    void notLoyalWhenVisitedOnlyDayX() {
        CustomerActivity a = new CustomerActivity("cust_001");
        a.recordDayX("/home");
        a.recordDayX("/about");

        assertThat(a.isLoyal()).isFalse();
    }

    @Test
    void notLoyalWhenVisitedOnlyDayY() {
        CustomerActivity a = new CustomerActivity("cust_001");
        a.recordDayY("/home");
        a.recordDayY("/about");

        assertThat(a.isLoyal()).isFalse();
    }

    @Test
    void notLoyalWhenOnlyOneUniquePage() {
        CustomerActivity a = new CustomerActivity("cust_001");
        a.recordDayX("/home");
        a.recordDayY("/home");

        assertThat(a.isLoyal()).isFalse();
    }

    @Test
    void notLoyalWhenNoActivity() {
        CustomerActivity a = new CustomerActivity("cust_001");

        assertThat(a.isLoyal()).isFalse();
    }
}
