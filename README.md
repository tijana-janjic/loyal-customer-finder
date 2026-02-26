# loyalty-analyzer-in-memory

Finds loyal customers from two daily web-app log files by loading both files into memory.

A customer is loyal if they appear on both days and visit at least 2 unique pages in total.

## Algorithm

Loads both files fully into memory and aggregates activity per customer:

1. Parse day X — record each customer's visited pages.
2. Parse day Y — extend the same customer activity records.
3. Evaluate loyalty: customer must have visited on both days and accumulated at least 2 unique pages.

An alternative implementation (`StreamingLoyaltyAnalyzer`) is also included for reference.
It processes the files sequentially with early termination, using less memory at the cost of added complexity.

## Log format

Each line must contain three whitespace-separated fields:

```
<timestamp> <pageId> <customerId>
```

Example:

```
2024-01-15T10:00:00 /home cust_001
2024-01-15T10:05:00 /about cust_001
```

Blank lines and malformed lines are skipped with a warning.

## Build and run

```
mvn package
java -jar target/loyalty-analyzer-in-memory-1.0.0.jar day_x.log day_y.log
```

## Test

```
mvn test
```
