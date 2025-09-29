package manning.performance.wordservice;

import manning.performance.wordservice.initial.DefaultWordsService;
import manning.performance.wordservice.optimized.CachedWordsService;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * JMH Benchmark comparing original vs optimized wordExists implementations.
 * 
 * This benchmark demonstrates the performance improvement achieved by:
 * 1. Caching the dictionary in a HashSet for O(1) lookups
 * 2. Eliminating file I/O operations for each word validation
 * 3. Expected improvement: 90-99% reduction in response time
 * 
 * To run this benchmark:
 * {@link WordExistsPerformanceTestRunner#main(String[])} method}
 */
@Fork(1)
@Warmup(iterations = 1)
@Measurement(iterations = 2)
@BenchmarkMode({Mode.AverageTime, Mode.Throughput})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class WordExistsPerformanceBenchmark {
    
    // Test data representing realistic word validation scenarios
    private static final int NUMBER_OF_CHECKS = 100;
    private static final List<String> WORDS_TO_CHECK = Arrays.asList(
        "made", "ask", "find", "zones", "1ask", "123",           // Original test words
        "cat", "house", "dog", "tree", "water", "fire",          // Common words (likely to exist)
        "nonexistentword", "xyzabc", "qwerty123",                // Non-existent words
        "aardvark", "zebra", "elephant", "rhinoceros",           // Dictionary words
        "flaggery", "breathable", "uniarticular", "beneficiaries" // Words from test data
    );

    private Path wordsPath;
    private CachedWordsService cachedService;

    @Setup(Level.Trial)
    public void setup() {
        wordsPath = getWordsPath();
        // Pre-load the cached service to avoid initialization overhead in benchmarks
        cachedService = new CachedWordsService(wordsPath);
    }

    /**
     * Baseline benchmark: Original implementation with file I/O for each lookup
     * This represents the current hot path performance issue
     */
    @Benchmark
    public void baseline_originalImplementation(Blackhole blackhole) {
        WordsService defaultWordsService = new DefaultWordsService(wordsPath);
        for (int i = 0; i < NUMBER_OF_CHECKS; i++) {
            for (String word : WORDS_TO_CHECK) {
                blackhole.consume(defaultWordsService.wordExists(word));
            }
        }
    }

    /**
     * Optimized benchmark: Cached implementation with HashSet O(1) lookups
     * This demonstrates the performance improvement from caching
     */
    @Benchmark
    public void optimized_cachedImplementation(Blackhole blackhole) {
        for (int i = 0; i < NUMBER_OF_CHECKS; i++) {
            for (String word : WORDS_TO_CHECK) {
                blackhole.consume(cachedService.wordExists(word));
            }
        }
    }

    /**
     * Single lookup benchmark: Compare single word validation performance
     * This isolates the core lookup performance difference
     */
    @Benchmark
    public void singleLookup_original(Blackhole blackhole) {
        WordsService defaultWordsService = new DefaultWordsService(wordsPath);
        blackhole.consume(defaultWordsService.wordExists("aardvark"));
    }

    @Benchmark
    public void singleLookup_optimized(Blackhole blackhole) {
        blackhole.consume(cachedService.wordExists("aardvark"));
    }

    /**
     * Cache initialization benchmark: Measure one-time setup cost
     * This shows the upfront cost of caching vs ongoing benefits
     */
    @Benchmark
    public void cacheInitialization(Blackhole blackhole) {
        CachedWordsService service = new CachedWordsService(wordsPath);
        blackhole.consume(service.getCacheStats());
    }

    private Path getWordsPath() {
        try {
            return Paths.get(
                    Objects.requireNonNull(getClass().getClassLoader().getResource("words.txt")).toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid words.txt path", e);
        }
    }
}
