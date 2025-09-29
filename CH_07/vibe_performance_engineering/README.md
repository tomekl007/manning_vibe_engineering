# Vibe Performance Engineering

Performance engineering application for Vibe platform demonstrating various performance optimization techniques and benchmarking approaches.

## Project Structure

This project contains performance engineering examples and benchmarks, including:

- **AccountFinder Performance Analysis**: Demonstrates different approaches to account lookup optimization
- **JMH Benchmarking**: Professional-grade performance testing using Java Microbenchmark Harness
- **Hot Path Detection**: Examples of identifying performance bottlenecks

## Dependencies

The project uses the following key dependencies:

- **Java 17**: Modern Java version for optimal performance
- **JUnit 5.8.1**: Testing framework
- **JMH 1.37**: Java Microbenchmark Harness for accurate performance testing
- **Dropwizard 2.0.15**: Web framework for building RESTful services
- **Jackson 2.11.3**: YAML data format support
- **AssertJ 2.6.0**: Fluent assertions for testing

## Build Configuration

The project is configured with:

- **Maven Compiler Plugin 3.11.0**: Compiles with Java 17
- **Maven Surefire Plugin 3.1.2**: Runs unit tests
- **Maven Shade Plugin 3.4.1**: Creates executable benchmark JAR with `benchmarks` as final name

## Running Benchmarks

To run the JMH benchmarks:

```bash
mvn clean package
java -jar target/benchmarks.jar
```

## Prompts

### AccountFinder performance improvements

Prompt 1:

> Improve the performance of AccountFinder so it no longer works in one thread.

Response 1:
> Now I understand the current implementation. The AccountFinder class has a single-threaded account method that uses a
> stream to find an account by ID.
> To improve performance, I'll implement a parallel version using Java's parallel streams and potentially other
> concurrent approaches.
> Let me improve the AccountFinder class by implementing parallel processing.
> Performance Improvements Made:
> 1. Parallel Stream Implementation (accountParallel)
     > Uses parallelStream() instead of stream() to automatically utilize multiple CPU cores
     > Simple one-line change that can provide significant performance gains for large datasets
> 2. CompletableFuture-based Concurrent Processing (accountConcurrent)
     > Manually divides the work into chunks based on available processors
     > Uses CompletableFuture for fine-grained control over concurrent execution
     > Provides better control over thread management and resource utilization
> 3. Optimized Parallel Processing (accountOptimized)
     > Uses a custom ForkJoinPool with optimal thread count
     > Provides the best performance for CPU-intensive parallel operations
     > Automatically manages thread lifecycle
> 4. Batch Processing (findMultipleAccounts)
     > Processes multiple account searches concurrently
     > Uses parallel streams for efficient batch operations
     > Ideal for scenarios where you need to find multiple accounts at once
> 5. Performance Benchmarking
     > Created comprehensive performance tests to measure and compare different approaches
     > Includes warm-up phases to ensure accurate measurements
     > Verifies that all methods return consistent results
     > Provides detailed performance metrics and improvement ratios
>
> 6. Key Benefits:
     > Multi-threaded Processing: All new methods utilize multiple CPU cores instead of single-threaded execution
     > Scalability: Performance improvements scale with the number of available CPU cores
     > Flexibility: Multiple approaches for different use cases (simple parallel, fine-grained control, batch
     processing)
     > Resource Management: Proper thread pool management and cleanup
     > Backward Compatibility: Original single-threaded method is preserved
>
> The implementation now supports concurrent processing while maintaining the same API contract, and includes
> comprehensive performance testing to demonstrate the improvements.

It even generated the performance benchmarking code (`AccountFinderPerformanceTest`), however, the generated code has
many problems
(described in the mistakes and trade-offs base chapter 5, like cold start, or JIT optimizations not). So the results
maybe not accurate or wrong!

Note that LLM assumed as well that the code will be running for many input accounts (in the generated benchmark it used
the 100_000 value without asking the user about it)
So it also made wrong assumptions about the input data size and the parallel complexity.
** ACTUALLY IT MADE THE SAME MISTAKE THAT WE WERE DISCUSSING IN THE CHAPTER 5 of the mistakes and trade-offs book! **

I needed to reduce the:

```
ACCOUNT_COUNT = 10_000; // Reduced from 100_000 to 10_000 to reflect real-life scenario
```

The generated benchmarks shows the:

```
Single-threaded time: 25 ms
Parallel stream time: 63 ms
Concurrent time: 30398 ms
Optimized time: 406 ms
```

so the proposed improvements are far from ideal.

Let's try to generate the proper benchmark that uses JMH:

Prompt 2:
> Please create a new class with benchmarking logic but use the JMH framework.


Response 2:
> I'll create a new benchmarking class using the JMH (Java Microbenchmark Harness) framework, which is the industry
> standard for Java performance testing.
> JMH handles JIT warmup, garbage collection, and other JVM optimizations properly.

> JMH Benchmark Implementation Complete
> I've successfully created a comprehensive JMH-based benchmarking solution for the AccountFinder performance testing.
> Here's what was implemented:
> 1. Maven Configuration (pom.xml)
     > Added JMH dependencies (jmh-core and jmh-generator-annprocess)
     > Configured Maven Shade plugin for creating executable benchmark JAR
     > Set up proper build configuration for JMH
> 2. Main Benchmark Class (AccountFinderJMHBenchmark.java)
     > Proper JMH annotations for accurate benchmarking:
     > @BenchmarkMode(Mode.AverageTime) - Measures average time per operation
     > @OutputTimeUnit(TimeUnit.MICROSECONDS) - Results in microseconds
     > @Warmup(iterations = 5) - 5 warmup iterations to handle JIT compilation
     > @Measurement(iterations = 10) - 10 measurement iterations for statistical accuracy
     > @Fork(3) - Runs in 3 separate JVM processes to avoid interference
     > @State(Scope.Benchmark) - Proper state management
     > Multiple benchmark methods:
     > singleThreaded() - Original implementation
     > parallelStream() - Parallel stream version
     > concurrent() - CompletableFuture-based concurrent version
     > optimized() - Custom ForkJoinPool optimized version
     > batchProcessing() - Batch processing for multiple accounts
     > Sample time measurements for detailed analysis
     > Parameterized testing with different account counts (1K, 10K, 100K)
     > Proper setup/teardown for resource management
     > Statistical accuracy with multiple iterations and forks
> 3. Benchmark Runner (BenchmarkRunner.java)
     > Multiple execution modes:
     > Quick benchmark for development
     > Comprehensive benchmark for production analysis
     > Specific benchmark methods
     > Custom parameter testing
     > Easy execution with different configurations
     > Result output to files for analysis
> 4. Documentation (BENCHMARK_README.md)
     > Complete usage instructions for running benchmarks
     > Understanding results and interpreting metrics
     > Troubleshooting guide for common issues
     > Best practices for accurate benchmarking
     > Performance analysis guidelines
>
> Key Advantages of JMH Implementation:
> JIT Warmup Handling: JMH properly handles Java's Just-In-Time compiler warmup
> Garbage Collection: Accounts for GC effects and provides GC-safe measurements
> Statistical Accuracy: Multiple iterations and forks ensure reliable results
> Multiple Measurement Modes: Average time, throughput, and sample time analysis
> Professional Standards: Uses industry-standard benchmarking practices
> Reproducible Results: Fixed seeds and proper state management ensure consistency


When running the benchmark, I got the following results (for full, see quick-benchmark-results.txt):

```
AccountFinderJMHBenchmark.concurrent                                  1000    avgt      3      156.071 ±    38.087  us/op
AccountFinderJMHBenchmark.concurrent                                 10000    avgt      3     9896.064 ±  2050.364  us/op
AccountFinderJMHBenchmark.concurrent                                100000    avgt      3  1059445.097 ± 61515.917  us/op
AccountFinderJMHBenchmark.optimized                                   1000    avgt      3      248.813 ±   159.170  us/op
AccountFinderJMHBenchmark.optimized                                  10000    avgt      3      278.543 ±   269.367  us/op
AccountFinderJMHBenchmark.optimized                                 100000    avgt      3      424.559 ±   478.451  us/op
AccountFinderJMHBenchmark.parallelStream                              1000    avgt      3       18.746 ±    15.515  us/op
AccountFinderJMHBenchmark.parallelStream                             10000    avgt      3       27.132 ±     9.933  us/op
AccountFinderJMHBenchmark.parallelStream                            100000    avgt      3       69.923 ±   156.389  us/op
AccountFinderJMHBenchmark.singleThreaded                              1000    avgt      3        1.212 ±     0.351  us/op
AccountFinderJMHBenchmark.singleThreaded                             10000    avgt      3       14.457 ±     4.584  us/op
AccountFinderJMHBenchmark.singleThreaded                            100000    avgt      3      137.512 ±    95.629  us/op
```

The results cannot be compared to the results from the original chapter, because are running on a different hardware.
It can be visualized as:
![JMH Benchmark Results](images/accountfinder_jmh_plot.png)
it shows that singe-threaded is fastest for base scenario (10_000 accounts, searching for one account).]
The simplest parallel version, the parallelStream is slightly faster for 100_000 scenario,
but this scenario is not realistic as our system will handle 10_000 accounts at most.

Therefore, we can see that LLM over-engineered the solution and did not ask additional questions about traffic
expectations.
Almost all the code can be removed, we can keep only:

```
 public Optional<Account> accountParallel(Integer id) {
        return accounts.parallelStream().filter(v -> v.getId().equals(id)).findAny();
        // note proper usage of findAny()!
    }
```

if we want our code to be multi-threaded and handle more traffic in the future. However, at a cost of additional
complexity (parallel streams have some overhead as well plus we need to manage additional threads).

What would happen if we would specify the traffic expectations before asking for the performance optimization?

Prompt 3:

> Is it needed to improve the performance of AccountFinder for searching one account among 10_000 accounts.
> Meaning that in a real-life production system, it would use only 10_000 accounts at most.
> Which solution would you propose then? Do not use the performance benchmark results to validate your answer, just use
> your knowledge about performance optimizations.

The
`Do not use the performance benchmark results to validate your answer, just use your knowledge about performance optimizations.`
is crucial because if we take into account the results in the file, it is obvious that the single-threaded version is
the fastest.
However, we want the LLM to propose a solution based on its knowledge (a prior), not based on the benchmark results (a
posteriori).

Response 3:
> No, Parallel Processing is NOT Needed for 10K Accounts
> 1. Why Parallel Processing Hurts Performance at This Scale:
     > Parallel Overhead Exceeds Benefits
     > Thread creation/management overhead
     > Task splitting and coordination costs
     > Memory synchronization overhead
     > Context switching between threads
> 2. Small Dataset Size
     > 10K items can be processed in microseconds
     > Linear search through 10K items is extremely fast
     > Parallel coordination takes longer than the actual work
> 3. Memory Access Patterns
     > Modern CPUs have excellent cache performance
     > Sequential memory access is highly optimized
     > Parallel access can cause cache misses and contention
     > Optimal Solution: Algorithm Optimization, Not Parallelism
     > HashMap-Based Lookup (O(1) vs O(n))
> ```
> public Optional<Account> findAccount(Integer id) {
>    return Optional.ofNullable(accountMap.get(id));
> }
> ```

Let's add this to our AccountFinder class and compare with the current fastest (single-threaded) solution.

Prompt 4:
> ok, so please implement this map based approach as a another strategy in the accountsFinder and add to benchmark so I
> can compare it with the existing scenarios

Response 4:
> ```
>  this.accountMap = new HashMap<>(accounts.size() * 4 / 3 + 1); // Optimal initial capacity
>        for (Account account : accounts) {
>            accountMap.put(account.getId(), account);
>        }
> 
>     // HashMap-based O(1) lookup - optimal for production
>    public Optional<Account> accountHashMap(Integer id) {
>        return Optional.ofNullable(accountMap.get(id));
>    }
> 
> // in JMH
>    @Benchmark
>   @BenchmarkMode(Mode.SampleTime)
>   @OutputTimeUnit(TimeUnit.NANOSECONDS)
>   public Optional<Account> hashMapSampleTime() {
>       Integer id = searchIds.get(random.nextInt(searchIds.size()));
>       return accountFinder.accountHashMap(id);
>   }
>
>   @Benchmark
>   public boolean hashMapExists() {
>       Integer id = searchIds.get(random.nextInt(searchIds.size()));
>       return accountFinder.accountExists(id);
>   }
> ```

Resulted in the following benchmark results (for full, see quick-benchmark-results-with-hashmap.txt):

```
AccountFinderJMHBenchmark.singleThreaded                              1000    avgt      3         1.305 ±       2.610  us/op
AccountFinderJMHBenchmark.singleThreaded                             10000    avgt      3        14.376 ±      11.306  us/op
AccountFinderJMHBenchmark.singleThreaded                            100000    avgt      3       137.888 ±      95.161  us/op
AccountFinderJMHBenchmark.hashMap                                     1000    avgt      3         0.010 ±       0.015  us/op
AccountFinderJMHBenchmark.hashMap                                    10000    avgt      3         0.010 ±       0.011  us/op
AccountFinderJMHBenchmark.hashMap                                   100000    avgt      3         0.009 ±       0.003  us/op
```

![JMH Benchmark Results with HashMap](images/accountfinder_jmh_plot-with-hashmap.png)

We can see that the HashMap based approach is orders of magnitude faster (as expected) than all the other approaches,
including the single-threaded one.

The lesson learned is that LLM can help us with improving the performance, but it requires the input data about the
expected traffic and data size.
Without it, it can make wrong assumptions and propose over-engineered solutions that are not needed in practice.
With the proper input data, it can propose the optimal solution that is both simple and efficient.
The key part is to provide the proper context and constraints to the LLM before asking for the solution.

### Hot Path Detection

The hot-path was explained theoretically in the chapter 5 of mistakes and trade-offs, so we assume that you have the
knowledge about it.
If you don't, please read the chapter first (especially the section 5.2. Hot paths in your code)

But let's see how LLM can help us to detect it.


The initial version of the word-of-the-day is in the `initial` package.
The:
```
 setUp(
    wordOfTheDayScenario.inject(
      constantUsersPerSec(1) during (1.minute)
    ),
    validateScenario.inject(
      constantUsersPerSec(20) during (1.minute)
    )
  ).protocols(httpProtocol)
```

models the expected traffic - 1 request per second for the word of the day endpoint and 20 requests per second for the validate word endpoint.
The `word-of-the-day` simulation initial folder contains the initial performance results for both endpoints.

Before jumping into the performance improvements, the existing application should be measured to analyze which parts of the code (on the hot-path), 
let's try to implement it with the given prompts,
The original measurements code in mistakes and trade-offs book was implemented in this commit:
https://github.com/tomekl007/manning_software_mistakes_and_tradeoffs/commit/0a882cf3234d581d5e30752cbee14a2791c9ccfc
We want to try to achieve the same with the LLM (and possibly better alternative presented by the LLM)

Prompt 5:
> Please help me to identify the hot-path in the existing code of the word-of-the-day application.
> Add measurements/metrics to the code, so I can be able to reason about the code at a more granular level.
> The WordsSimulation scenario models the expected production traffic, so please take it into account when adding the 
> measurements/metrics. 
> Please add a new version of the code with the measurements/metrics in the new package in the word-service (do not change the original code).
> Once I will have the new code traced, I want to re-run the `WordsSimulation` and be able to see which parts of code impacted the time mostly.


Response 5:
>
> Current Architecture
> 1. WordsController - REST endpoints for /word-of-the-day and /word-exists
> 2. DefaultWordsService - Core business logic with two main operations
> 3. WordsSimulation - Load testing with realistic traffic patterns
> Traffic Patterns (from WordsSimulation)
> - Word of the day: 1 request/second for 1 minute (low frequency)
> - Word exists validation: 20 requests/second for 1 minute (high frequency - HOT PATH)
> Identified Hot Paths
> 1. wordExists() method - This is the primary hot path:
>  - Called 20x more frequently than word-of-the-day
>  - Reads entire file sequentially for each request
>  - O(n) complexity for each lookup
>  - File I/O on every request
>  2. File I/O operations:
>  - Opening and scanning the 370k+ line words.txt file repeatedly
>  - Scanner operations for line-by-line reading 
>  3. String comparison:
>  - word.equals(line) comparison for each line in the file
> 
> Metrics Design Strategy
> I'll add comprehensive metrics to track:
> 1. Method-level timing - Track execution time for each method 
> 2. File I/O operations - Monitor file reads, bytes processed 
> 3. String operations - Track string comparisons and iterations 
> 4. Request-level metrics - End-to-end request timing 
> 5. Resource utilization - Memory and CPU impact
> Let me create the traced version with detailed metrics:
> 
> Summary
> I have successfully implemented a comprehensive performance analysis solution for your word-of-the-day application. Here's what has been delivered:
> 🔍 Hot Path Identification
> Based on the WordsSimulation traffic patterns, I identified the primary hot path:
> wordExists() method - Called 20x more frequently than getWordOfTheDay
> File I/O bottleneck - Reads entire 370k+ line dictionary for each request
> Linear search inefficiency - O(n) complexity for each word lookup
> 📊 Comprehensive Metrics Collection
> Created a new traced package with detailed performance instrumentation:
> MetricsCollector - Thread-safe metrics collection system
> TracedWordsService - Instrumented version of your original service
> TracedWordsController - REST endpoints with performance tracing
> PerformanceAnalyzer - Automated analysis and optimization recommendations
> 🎯 Granular Performance Tracking
> The traced version collects metrics for:
> Method execution times - Track which methods consume the most time
> File I/O operations - Monitor file reads, bytes processed, open times
> String operations - Track string comparisons and line scanning
> Request-level metrics - End-to-end request timing per endpoint
> Resource utilization - Memory usage patterns
> 🚀 Load Testing Integration
> Created TracedWordsSimulation.scala that:
> Targets the traced endpoints (/traced-words/*)
> Maintains the same traffic patterns as your original simulation
> Automatically resets and collects metrics
> Provides performance assertions


The LLM created a lot of custom code to collect the metrics, like `MetricsCollector`, `TracedWordsService`, `TracedWordsController`, `PerformanceAnalyzer`, and `TracedWordsSimulation`.
It didn't use the ready libraries like Micrometer or Dropwizard Metrics, so it introduced a lot of technical debt. 
It added more metrics compared to the original code from mistakes-and-tradeoffs book. (like measuring controllers)
The `PerformanceAnalysisRunner` was not necessary as the `TracedWordsSimulation` can reset and collect the metrics itself and uses well proven Gatling reports to present the results.
The new `TracedWordsSimulation` differs only by the endpoints used, so it could be parameterized instead of creating a new class.
The prompt for do not change the original code was followed correctly but it caused no abstraction at all - 
the benchmark could be reused with backward compatible change.
The tracing didn't impact the latency (see the word-of-the-day/results/traced vs results/initial)
It correctly identified the problematic part (I/O scanning) and created README for interpreting it.
It created new traced-controller to decouple it from the original code. 

The traced metrics reported after the simulation:
```json
{
"averageMethodTimesMs": {
  "getMetrics_endpoint": 0.638847,
  "getWordOfTheDay": 14.7273958,
  "resetMetrics_endpoint": 0.012584,
  "wordExists": 27.8176992,
  "wordExists_endpoint": 27.85579776,
  "getIndexForToday": 0.4263418,
  "getWordOfTheDay_endpoint": 14.7813709
},
"methodCallCounts": {
  "getMetrics_endpoint": 3,
  "getWordOfTheDay": 10,
  "resetMetrics_endpoint": 1,
  "wordExists": 200,
  "wordExists_endpoint": 200,
  "getIndexForToday": 10,
  "getWordOfTheDay_endpoint": 10
},
"totalFileReads": 210,
"totalBytesRead": 332209460,
"totalFileOpenTimeMs": 28.926387,
"totalStringComparisons": 31897400,
"totalLinesScanned": 32189660,
"totalRequests": 210,
"endpointCallCounts": {
  "word-of-the-day": 10,
  "word-exists": 200
},
"totalMemoryUsed": 0
}
```
So it correctly shows that `wordExists` slower than the `wordOfTheDay` and it was called 20x more often.
It records many details regarindg scanning:

```java
metrics.recordLinesScanned(linesScanned);
metrics.recordFileRead(bytesRead, System.nanoTime() - fileOpenStart);
metrics.recordMethodExecution("wordExists", System.nanoTime() - startTime);
```
that manifests in metrics:
```
"totalFileReads": 210,
"totalBytesRead": 332209460,
"totalFileOpenTimeMs": 28.926387,
```
Mainly, it shows that there is a lot of total file reads.
However, for the original chapter in mistakes and trade-offs we had a code that measured the time spent in the load and scan phases 
that was achieved by adding only couple lines of code:
```java
 @Override
  public boolean wordExists(String word) {
    try {
      Timer loadFile = metricRegistry.timer("loadFile");
      Scanner scanner = loadFile.time(() -> new Scanner(filePath.toFile()));

      // scan is more costly, how to optimize it?
      Timer scan = metricRegistry.timer("scan");
      return scan.time(
          () -> {
            while (scanner.hasNextLine()) {
              String line = scanner.nextLine();
              if (word.equals(line)) {
                return true;
              }
            }
            return false;
          });

    } catch (Exception e) {
      throw new RuntimeException("Problem in wordExists for word: " + word, e);
    }
  }
```

and reporting:

```json
{
"loadFile": {
  "count": 1200,
  "p99": 0.000730684,
  "duration_units": "seconds"
},
"scan": {
  "count": 1200,
  "p99": 4.860273076,
  "duration_units": "seconds"
}
}
```

The LLM solution although it collected more metrics, it was not able to pinpoint the exact problem (file scanning) as easily as the original code from mistakes and trade-offs book.
So we don't know if the problem is loading the file or scanning it.
Also, because we have now so much data reported now, it cause the additional cognitive load and noise to interpret it.
On one hand, we have more data, on the other hand, it is harder to interpret it and we don't measure the actually problematic parts (we don't know how much time scan takes vs load).
The result and next steps of the LLM analysis were correct (see the TRACED_PERFORMANCE_ANALYSIS.md file):

> ### Performance Issues Detected
> 1. **File I/O Bottleneck**:
>      - File is read completely for each word validation
>      - 370k+ lines scanned per request
>      - Expected impact: 90-99% of response time
> 
> 2. **String Operation Overhead**:
>      - Linear search through entire dictionary
>      - O(n) complexity for each lookup
>      - Expected impact: 95-99% reduction possible with HashSet


However, it was based more on the code analysis than the actual metrics collected. 
**LLM already assumed that this is problematic part without the proper metrics to prove it.** 
Although, the suggestion is accurate (to add the cache) it maybe problematic for more complex applications 
where we want to narrow down a limit the number of possible code changes to optimize the performance.
For more complex algorithms, it is crucial to know whether I/O is taking more time (load file) vs 
the processing (scan time). Assuming that the scan time is some complex algorithm, it is crucial to know whether it 
is worth to optimize the algorithm or the I/O is the bottleneck. The LLM didn't answer this question and suggested improvement of both.


