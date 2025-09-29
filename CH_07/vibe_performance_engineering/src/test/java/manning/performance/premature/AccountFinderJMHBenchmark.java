package manning.performance.premature;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * JMH-based benchmark for AccountFinder performance comparison.
 * This benchmark properly handles JIT warmup, garbage collection, and other JVM optimizations.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@Threads(1)
public class AccountFinderJMHBenchmark {

    @Param({"1000", "10000", "100000"})
    private int accountCount;

    private AccountFinder accountFinder;
    private List<Account> accounts;
    private List<Integer> searchIds;
    private Random random;

    @Setup(Level.Trial)
    public void setupTrial() {
        random = new Random(42); // Fixed seed for reproducible results
        accounts = generateAccounts(accountCount);
        accountFinder = new AccountFinder(accounts);
        searchIds = generateRandomIds(random, 100);
    }

    @Setup(Level.Iteration)
    public void setupIteration() {
        // Shuffle search IDs for each iteration to avoid cache effects
        searchIds = generateRandomIds(random, 100);
    }

    @TearDown(Level.Trial)
    public void tearDownTrial() {
        if (accountFinder != null) {
            accountFinder.shutdown();
        }
    }

    @Benchmark
    public Optional<Account> singleThreaded() {
        Integer id = searchIds.get(random.nextInt(searchIds.size()));
        return accountFinder.account(id);
    }

    @Benchmark
    public Optional<Account> parallelStream() {
        Integer id = searchIds.get(random.nextInt(searchIds.size()));
        return accountFinder.accountParallel(id);
    }

    @Benchmark
    public Optional<Account> concurrent() {
        Integer id = searchIds.get(random.nextInt(searchIds.size()));
        return accountFinder.accountConcurrent(id);
    }

    @Benchmark
    public Optional<Account> optimized() {
        Integer id = searchIds.get(random.nextInt(searchIds.size()));
        return accountFinder.accountOptimized(id);
    }

    @Benchmark
    public Optional<Account> hashMap() {
        Integer id = searchIds.get(random.nextInt(searchIds.size()));
        return accountFinder.accountHashMap(id);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public List<Optional<Account>> batchProcessing() {
        return accountFinder.findMultipleAccounts(searchIds.subList(0, 10));
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public List<Optional<Account>> batchProcessingHashMap() {
        return accountFinder.findMultipleAccountsHashMap(searchIds.subList(0, 10));
    }

    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public Optional<Account> singleThreadedSampleTime() {
        Integer id = searchIds.get(random.nextInt(searchIds.size()));
        return accountFinder.account(id);
    }

    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public Optional<Account> parallelStreamSampleTime() {
        Integer id = searchIds.get(random.nextInt(searchIds.size()));
        return accountFinder.accountParallel(id);
    }

    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public Optional<Account> hashMapSampleTime() {
        Integer id = searchIds.get(random.nextInt(searchIds.size()));
        return accountFinder.accountHashMap(id);
    }

    @Benchmark
    public boolean hashMapExists() {
        Integer id = searchIds.get(random.nextInt(searchIds.size()));
        return accountFinder.accountExists(id);
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
            ids.add(random.nextInt(accountCount));
        }
        return ids;
    }

    /**
     * Main method to run the benchmark.
     * Can be executed directly or via Maven.
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(AccountFinderJMHBenchmark.class.getSimpleName())
                .result("benchmark-results.txt")
                .resultFormat(ResultFormatType.TEXT)
                .build();

        new Runner(opt).run();
    }
}
