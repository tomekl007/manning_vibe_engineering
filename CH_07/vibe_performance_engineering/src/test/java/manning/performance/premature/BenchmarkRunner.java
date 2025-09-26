package manning.performance.premature;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.VerboseMode;

/**
 * Simple runner class to execute JMH benchmarks with different configurations.
 * This makes it easier to run benchmarks with various options.
 */
public class BenchmarkRunner {

    public static void main(String[] args) throws RunnerException {
        // Quick benchmark with minimal iterations for development
        runQuickBenchmark();
        
        // Uncomment to run comprehensive benchmark
        // runComprehensiveBenchmark();
        
        // Uncomment to run specific benchmark methods
        // runSpecificBenchmarks();
    }

    /**
     * Quick benchmark for development and testing.
     * Uses minimal iterations and warmup for faster execution.
     */
    public static void runQuickBenchmark() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(AccountFinderJMHBenchmark.class.getSimpleName())
                .warmupIterations(2)
                .measurementIterations(3)
                .forks(1)
                .verbosity(VerboseMode.NORMAL)
                .result("quick-benchmark-results.txt")
                .resultFormat(org.openjdk.jmh.results.format.ResultFormatType.TEXT)
                .build();

        System.out.println("Running quick benchmark...");
        new Runner(opt).run();
    }

    /**
     * Comprehensive benchmark with full iterations.
     * Use this for production performance analysis.
     */
    public static void runComprehensiveBenchmark() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(AccountFinderJMHBenchmark.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(10)
                .forks(3)
                .verbosity(VerboseMode.EXTRA)
                .result("comprehensive-benchmark-results.txt")
                .resultFormat(org.openjdk.jmh.results.format.ResultFormatType.TEXT)
                .build();

        System.out.println("Running comprehensive benchmark...");
        new Runner(opt).run();
    }

    /**
     * Run only specific benchmark methods.
     * Useful for testing individual implementations.
     */
    public static void runSpecificBenchmarks() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(AccountFinderJMHBenchmark.class.getSimpleName() + ".singleThreaded")
                .include(AccountFinderJMHBenchmark.class.getSimpleName() + ".parallelStream")
                .warmupIterations(3)
                .measurementIterations(5)
                .forks(2)
                .verbosity(VerboseMode.NORMAL)
                .result("specific-benchmark-results.txt")
                .resultFormat(org.openjdk.jmh.results.format.ResultFormatType.TEXT)
                .build();

        System.out.println("Running specific benchmarks...");
        new Runner(opt).run();
    }

    /**
     * Run benchmark with custom parameters.
     * Allows testing with different account counts.
     */
    public static void runCustomBenchmark() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(AccountFinderJMHBenchmark.class.getSimpleName())
                .param("accountCount", "5000", "50000", "500000")
                .warmupIterations(3)
                .measurementIterations(5)
                .forks(2)
                .verbosity(VerboseMode.NORMAL)
                .result("custom-benchmark-results.txt")
                .resultFormat(org.openjdk.jmh.results.format.ResultFormatType.TEXT)
                .build();

        System.out.println("Running custom benchmark...");
        new Runner(opt).run();
    }
}
