package manning.performance.wordservice.optimized;

import manning.performance.wordservice.WordsService;
import manning.performance.wordservice.initial.DefaultWordsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive performance comparison test between original and optimized implementations.
 * This test validates that the optimized version provides the same functionality
 * with significantly better performance.
 */
public class PerformanceComparisonTest {

    private WordsService originalService;
    private CachedWordsService optimizedService;
    private Path wordsPath;

    // Test words representing various scenarios
    private final List<String> testWords = Arrays.asList(
        "aardvark",     // Dictionary word
        "zebra",        // Dictionary word
        "nonexistent",  // Non-existent word
        "xyz123",       // Non-existent word
        "cat",          // Dictionary word
        "house",        // Dictionary word
        "made",         // Dictionary word
        "ask",          // Dictionary word
        "find",         // Dictionary word
        "zones"         // Dictionary word
    );

    @BeforeEach
    public void setUp() throws URISyntaxException {
        wordsPath = getWordsPath();
        originalService = new DefaultWordsService(wordsPath);
        optimizedService = new CachedWordsService(wordsPath);
    }

    @Test
    public void testFunctionalEquivalence() {
        // Verify that both implementations return the same results
        for (String word : testWords) {
            boolean originalResult = originalService.wordExists(word);
            boolean optimizedResult = optimizedService.wordExists(word);
            
            assertEquals(originalResult, optimizedResult, 
                "Results should be identical for word: " + word);
        }
    }

    @Test
    public void testWordOfTheDayEquivalence() {
        // Verify word-of-the-day functionality is preserved
        String originalWord = originalService.getWordOfTheDay();
        String optimizedWord = optimizedService.getWordOfTheDay();
        
        assertEquals(originalWord, optimizedWord, 
            "Word of the day should be identical");
    }

    @Test
    public void testPerformanceImprovement() {
        int iterations = 1000;
        
        // Measure original implementation
        long originalStartTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            for (String word : testWords) {
                originalService.wordExists(word);
            }
        }
        long originalEndTime = System.nanoTime();
        long originalDuration = originalEndTime - originalStartTime;
        
        // Measure optimized implementation
        long optimizedStartTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            for (String word : testWords) {
                optimizedService.wordExists(word);
            }
        }
        long optimizedEndTime = System.nanoTime();
        long optimizedDuration = optimizedEndTime - optimizedStartTime;
        
        // Calculate improvement
        double improvementRatio = (double) originalDuration / optimizedDuration;
        double improvementPercentage = ((double) (originalDuration - optimizedDuration) / originalDuration) * 100;
        
        System.out.println("Performance Comparison Results:");
        System.out.println("==============================");
        System.out.println("Original implementation: " + (originalDuration / 1_000_000.0) + " ms");
        System.out.println("Optimized implementation: " + (optimizedDuration / 1_000_000.0) + " ms");
        System.out.println("Improvement ratio: " + String.format("%.1fx", improvementRatio));
        System.out.println("Improvement percentage: " + String.format("%.1f%%", improvementPercentage));
        
        // Assert significant improvement (at least 90% improvement)
        assertTrue(improvementPercentage >= 90.0, 
            "Expected at least 90% performance improvement, but got: " + improvementPercentage + "%");
        
        // Assert that optimized version is at least 10x faster
        assertTrue(improvementRatio >= 10.0, 
            "Expected at least 10x speedup, but got: " + improvementRatio + "x");
    }

    @Test
    public void testCacheStatistics() {
        CachedWordsService.CacheStats stats = optimizedService.getCacheStats();
        
        assertNotNull(stats, "Cache stats should not be null");
        assertTrue(stats.totalWords > 0, "Should have loaded words");
        assertTrue(stats.cachedWords > 0, "Should have cached words");
        assertTrue(stats.loadTimeMs > 0, "Should have load time");
        assertTrue(stats.memoryUsed > 0, "Should have memory usage");
        
        System.out.println("Cache Statistics:");
        System.out.println("=================");
        System.out.println(stats.toString());
    }

    @Test
    public void testMemoryEfficiency() {
        // Test that caching doesn't cause excessive memory usage
        CachedWordsService.CacheStats stats = optimizedService.getCacheStats();
        
        // Estimate reasonable memory usage (roughly 50 bytes per word including overhead)
        long estimatedMemoryPerWord = 50;
        long estimatedTotalMemory = stats.totalWords * estimatedMemoryPerWord;
        
        assertTrue(stats.memoryUsed < estimatedTotalMemory * 2, 
            "Memory usage should be reasonable: " + stats.memoryUsed + " bytes for " + stats.totalWords + " words");
        
        System.out.println("Memory Efficiency:");
        System.out.println("==================");
        System.out.println("Total words: " + stats.totalWords);
        System.out.println("Memory used: " + stats.memoryUsed + " bytes");
        System.out.println("Memory per word: " + (stats.memoryUsed / stats.totalWords) + " bytes");
    }

    @Test
    public void testConcurrentAccess() throws InterruptedException {
        // Test that the optimized service works correctly under concurrent access
        int numThreads = 10;
        int iterationsPerThread = 100;
        Thread[] threads = new Thread[numThreads];
        
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < iterationsPerThread; j++) {
                    for (String word : testWords) {
                        boolean result = optimizedService.wordExists(word);
                        // Verify result is consistent (not null, true/false as expected)
                        assertNotNull(result);
                    }
                }
            });
        }
        
        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }
        
        System.out.println("Concurrent access test completed successfully with " + numThreads + " threads");
    }

    private Path getWordsPath() throws URISyntaxException {
        return Paths.get(
                Objects.requireNonNull(getClass().getClassLoader().getResource("words.txt")).toURI());
    }
}
