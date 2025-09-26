## Prompts

### AccountFinder performance improvements

Prompt 1:

> Improve the performance of AccountFinder so it no longer works in one thread.

Response 1:
> Now I understand the current implementation. The AccountFinder class has a single-threaded account method that uses a stream to find an account by ID. 
> To improve performance, I'll implement a parallel version using Java's parallel streams and potentially other concurrent approaches.
> Let me improve the AccountFinder class by implementing parallel processing.
> Performance Improvements Made:
> 1. Parallel Stream Implementation (accountParallel)
>    Uses parallelStream() instead of stream() to automatically utilize multiple CPU cores
>    Simple one-line change that can provide significant performance gains for large datasets
> 2. CompletableFuture-based Concurrent Processing (accountConcurrent)
>    Manually divides the work into chunks based on available processors
>    Uses CompletableFuture for fine-grained control over concurrent execution
>    Provides better control over thread management and resource utilization
> 3. Optimized Parallel Processing (accountOptimized)
>    Uses a custom ForkJoinPool with optimal thread count
>    Provides the best performance for CPU-intensive parallel operations
>    Automatically manages thread lifecycle
> 4. Batch Processing (findMultipleAccounts)
>    Processes multiple account searches concurrently
>    Uses parallel streams for efficient batch operations
>    Ideal for scenarios where you need to find multiple accounts at once
> 5. Performance Benchmarking
>    Created comprehensive performance tests to measure and compare different approaches
>    Includes warm-up phases to ensure accurate measurements
>    Verifies that all methods return consistent results
>    Provides detailed performance metrics and improvement ratios
>    
> 6. Key Benefits:
>    Multi-threaded Processing: All new methods utilize multiple CPU cores instead of single-threaded execution
>    Scalability: Performance improvements scale with the number of available CPU cores
>    Flexibility: Multiple approaches for different use cases (simple parallel, fine-grained control, batch processing)
>    Resource Management: Proper thread pool management and cleanup
>    Backward Compatibility: Original single-threaded method is preserved
>    
> The implementation now supports concurrent processing while maintaining the same API contract, and includes comprehensive performance testing to demonstrate the improvements.

It even generated the performance benchmarking code (`AccountFinderPerformanceTest`), however, the generated code has many problems 
(described in the mistakes and trade-offs base chapter 5, like cold start, or JIT optimizations not). So the results maybe not accurate or wrong!

Note that LLM assumed as well that the code will be running for many input accounts (in the generated benchmark it used the 100_000 value without asking the user about it)
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
> I'll create a new benchmarking class using the JMH (Java Microbenchmark Harness) framework, which is the industry standard for Java performance testing.
> JMH handles JIT warmup, garbage collection, and other JVM optimizations properly.

> JMH Benchmark Implementation Complete
> I've successfully created a comprehensive JMH-based benchmarking solution for the AccountFinder performance testing. Here's what was implemented:
> 1. Maven Configuration (pom.xml)
>    Added JMH dependencies (jmh-core and jmh-generator-annprocess)
>    Configured Maven Shade plugin for creating executable benchmark JAR
>    Set up proper build configuration for JMH
> 2. Main Benchmark Class (AccountFinderJMHBenchmark.java)
>    Proper JMH annotations for accurate benchmarking:
>    @BenchmarkMode(Mode.AverageTime) - Measures average time per operation
>    @OutputTimeUnit(TimeUnit.MICROSECONDS) - Results in microseconds
>    @Warmup(iterations = 5) - 5 warmup iterations to handle JIT compilation
>    @Measurement(iterations = 10) - 10 measurement iterations for statistical accuracy
>    @Fork(3) - Runs in 3 separate JVM processes to avoid interference
>    @State(Scope.Benchmark) - Proper state management
>    Multiple benchmark methods:
>    singleThreaded() - Original implementation
>    parallelStream() - Parallel stream version
>    concurrent() - CompletableFuture-based concurrent version
>    optimized() - Custom ForkJoinPool optimized version
>    batchProcessing() - Batch processing for multiple accounts
>    Sample time measurements for detailed analysis
>    Parameterized testing with different account counts (1K, 10K, 100K)
>    Proper setup/teardown for resource management
>    Statistical accuracy with multiple iterations and forks
> 3. Benchmark Runner (BenchmarkRunner.java)
>    Multiple execution modes:
>    Quick benchmark for development
>    Comprehensive benchmark for production analysis
>    Specific benchmark methods
>    Custom parameter testing
>    Easy execution with different configurations
>    Result output to files for analysis
> 4. Documentation (BENCHMARK_README.md)
>    Complete usage instructions for running benchmarks
>    Understanding results and interpreting metrics
>    Troubleshooting guide for common issues
>    Best practices for accurate benchmarking
>    Performance analysis guidelines
> 
> Key Advantages of JMH Implementation:
>    JIT Warmup Handling: JMH properly handles Java's Just-In-Time compiler warmup
>    Garbage Collection: Accounts for GC effects and provides GC-safe measurements
>    Statistical Accuracy: Multiple iterations and forks ensure reliable results
>    Multiple Measurement Modes: Average time, throughput, and sample time analysis
>    Professional Standards: Uses industry-standard benchmarking practices
>    Reproducible Results: Fixed seeds and proper state management ensure consistency


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

Therefore, we can see that LLM over-engineered the solution and did not ask additional questions about traffic expectations.
Almost all the code can be removed, we can keep only:
```
 public Optional<Account> accountParallel(Integer id) {
        return accounts.parallelStream().filter(v -> v.getId().equals(id)).findAny();
        // note proper usage of findAny()!
    }
```
if we want our code to be multi-threaded and handle more traffic in the future. However, at a cost of additional complexity (parallel streams have some overhead as well plus we need to manage additional threads). 

What would happen if we would specify the traffic expectations before asking for the performance optimization?

Prompt 3:

> Is it needed to improve the performance of AccountFinder for searching one account among 10_000 accounts. 
> Meaning that in a real-life production system, it would use only 10_000 accounts at most.
> Which solution would you propose then? Do not use the performance benchmark results to validate your answer, just use your knowledge about performance optimizations.

The `Do not use the performance benchmark results to validate your answer, just use your knowledge about performance optimizations.` 
is crucial because if we take into account the results in the file, it is obvious that the single-threaded version is the fastest.
However, we want the LLM to propose a solution based on its knowledge (a prior), not based on the benchmark results (a posteriori).

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
> ok, so please implement this map based approach as a another strategy in the accountsFinder and add to benchmark so I can compare it with the existing scenarios

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

We can see that the HashMap based approach is orders of magnitude faster (as expected) than all the other approaches, including the single-threaded one.

The lesson learned is that LLM can help us with improving the performance, but it requires the input data about the expected traffic and data size.
Without it, it can make wrong assumptions and propose over-engineered solutions that are not needed in practice.
With the proper input data, it can propose the optimal solution that is both simple and efficient.
The key part is to provide the proper context and constraints to the LLM before asking for the solution.


### Hot Path Detection

The hot-path was explained theoretically in the chapter 5 of mistakes and trade-offs, so we assume that you have the knowledge about it.
If you don't, please read the chapter first (especially the section 5.2. Hot paths in your code)

But let's see how LLM can help us to detect it.