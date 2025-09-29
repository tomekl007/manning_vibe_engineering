package manning.performance.premature;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This benchmark is LLM generated, and may need adjustments to work in your environment.
 * the generated code has many problems
 * (described in the mistakes and trade-offs base chapter 5, like cold start, or JIT optimizations not). So the results maybe not accurate or wrong!
 */

public class AccountFinderPerformanceTest {
    private AccountFinder accountFinder;
    private List<Account> accounts;
    private static final int ACCOUNT_COUNT = 10000;
    private static final int SEARCH_COUNT = 1000;

    @BeforeEach
    void setUp() {
        accounts = generateAccounts(ACCOUNT_COUNT);
        accountFinder = new AccountFinder(accounts);
    }

    @Test
    void testPerformanceComparison() {
        Random random = new Random();
        List<Integer> searchIds = generateRandomIds(random, SEARCH_COUNT);

        // Warm up JVM
        warmUp();

        // Test single-threaded performance
        long singleThreadTime = measureSingleThreadedPerformance(searchIds);
        System.out.println("Single-threaded time: " + singleThreadTime + " ms");

        // Test parallel stream performance
        long parallelTime = measureParallelPerformance(searchIds);
        System.out.println("Parallel stream time: " + parallelTime + " ms");

        // Test concurrent performance
        long concurrentTime = measureConcurrentPerformance(searchIds);
        System.out.println("Concurrent time: " + concurrentTime + " ms");

        // Test optimized performance
        long optimizedTime = measureOptimizedPerformance(searchIds);
        System.out.println("Optimized time: " + optimizedTime + " ms");

        // Verify all methods return the same results
        verifyResultsConsistency(searchIds);

        // Performance assertions
        assertTrue(parallelTime < singleThreadTime,
                "Parallel stream should be faster than single-threaded");
        assertTrue(concurrentTime < singleThreadTime,
                "Concurrent approach should be faster than single-threaded");
        assertTrue(optimizedTime < singleThreadTime,
                "Optimized approach should be faster than single-threaded");

        System.out.println("Performance improvement:");
        System.out.println("Parallel: " + String.format("%.2f", (double) singleThreadTime / parallelTime) + "x faster");
        System.out.println("Concurrent: " + String.format("%.2f", (double) singleThreadTime / concurrentTime) + "x faster");
        System.out.println("Optimized: " + String.format("%.2f", (double) singleThreadTime / optimizedTime) + "x faster");
    }

    @Test
    void testBatchProcessingPerformance() {
        Random random = new Random();
        List<Integer> searchIds = generateRandomIds(random, 100);

        long startTime = System.currentTimeMillis();
        List<Optional<Account>> results = accountFinder.findMultipleAccounts(searchIds);
        long endTime = System.currentTimeMillis();

        System.out.println("Batch processing time: " + (endTime - startTime) + " ms");
        assertEquals(searchIds.size(), results.size());

        // Verify all results are present
        assertTrue(results.stream().anyMatch(Optional::isPresent));
    }

    private void warmUp() {
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            Integer id = random.nextInt(ACCOUNT_COUNT);
            accountFinder.account(id);
            accountFinder.accountParallel(id);
            accountFinder.accountConcurrent(id);
            accountFinder.accountOptimized(id);
        }
    }

    private long measureSingleThreadedPerformance(List<Integer> searchIds) {
        long startTime = System.currentTimeMillis();
        for (Integer id : searchIds) {
            accountFinder.account(id);
        }
        return System.currentTimeMillis() - startTime;
    }

    private long measureParallelPerformance(List<Integer> searchIds) {
        long startTime = System.currentTimeMillis();
        for (Integer id : searchIds) {
            accountFinder.accountParallel(id);
        }
        return System.currentTimeMillis() - startTime;
    }

    private long measureConcurrentPerformance(List<Integer> searchIds) {
        long startTime = System.currentTimeMillis();
        for (Integer id : searchIds) {
            accountFinder.accountConcurrent(id);
        }
        return System.currentTimeMillis() - startTime;
    }

    private long measureOptimizedPerformance(List<Integer> searchIds) {
        long startTime = System.currentTimeMillis();
        for (Integer id : searchIds) {
            accountFinder.accountOptimized(id);
        }
        return System.currentTimeMillis() - startTime;
    }

    private void verifyResultsConsistency(List<Integer> searchIds) {
        for (Integer id : searchIds) {
            Optional<Account> singleThreadResult = accountFinder.account(id);
            Optional<Account> parallelResult = accountFinder.accountParallel(id);
            Optional<Account> concurrentResult = accountFinder.accountConcurrent(id);
            Optional<Account> optimizedResult = accountFinder.accountOptimized(id);

            assertEquals(singleThreadResult, parallelResult,
                    "Parallel result should match single-threaded result for ID: " + id);
            assertEquals(singleThreadResult, concurrentResult,
                    "Concurrent result should match single-threaded result for ID: " + id);
            assertEquals(singleThreadResult, optimizedResult,
                    "Optimized result should match single-threaded result for ID: " + id);
        }
    }

    private List<Account> generateAccounts(int count) {
        List<Account> accounts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            accounts.add(new Account("Account" + i, i));
        }
        return accounts;
    }

    private List<Integer> generateRandomIds(Random random, int count) {
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ids.add(random.nextInt(ACCOUNT_COUNT));
        }
        return ids;
    }
}
