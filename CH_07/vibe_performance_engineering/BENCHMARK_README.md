# JMH Benchmarking for AccountFinder

This project includes comprehensive JMH (Java Microbenchmark Harness) benchmarks to properly measure the performance of
different AccountFinder implementations.

## What is JMH?

JMH is the industry standard for Java microbenchmarking. It handles:

- JIT compiler warmup
- Garbage collection effects
- Thread synchronization
- Statistical analysis
- Multiple measurement modes

## Running the Benchmarks

### Method 1: Using the BenchmarkRunner class

```bash
# Quick benchmark (for development)
mvn compile exec:java -Dexec.mainClass="manning.performance.premature.BenchmarkRunner"

# Or run specific methods
mvn compile exec:java -Dexec.mainClass="manning.performance.premature.BenchmarkRunner" -Dexec.args="runQuickBenchmark"
```

### Method 2: Using Maven Shade Plugin

```bash
# Build the benchmark JAR
mvn clean package

# Run the benchmark
java -jar target/benchmarks.jar
```

### Method 3: Direct execution

```bash
# Run the main benchmark class directly
mvn compile exec:java -Dexec.mainClass="manning.performance.premature.AccountFinderJMHBenchmark"
```

## Benchmark Methods

The benchmark includes the following methods:

1. **singleThreaded()** - Original single-threaded implementation
2. **parallelStream()** - Parallel stream implementation
3. **concurrent()** - CompletableFuture-based concurrent implementation
4. **optimized()** - Custom ForkJoinPool optimized implementation
5. **batchProcessing()** - Batch processing for multiple accounts
6. **singleThreadedSampleTime()** - Sample time measurement for single-threaded
7. **parallelStreamSampleTime()** - Sample time measurement for parallel stream

## Benchmark Parameters

- **Account Count**: 1,000, 10,000, 100,000 accounts
- **Warmup**: 5 iterations, 1 second each
- **Measurement**: 10 iterations, 1 second each
- **Forks**: 3 (runs in separate JVM processes)
- **Threads**: 1 (single-threaded execution)

## Understanding the Results

### Measurement Modes

- **AverageTime**: Average time per operation in microseconds
- **Throughput**: Operations per second
- **SampleTime**: Distribution of operation times

### Key Metrics

- **Score**: The primary performance metric
- **Error**: Statistical error margin
- **Units**: Time unit (μs, ns, s)
- **Ops/sec**: Operations per second

### Example Output

```
Benchmark                                    (accountCount)  Mode  Cnt    Score   Error  Units
AccountFinderJMHBenchmark.singleThreaded              1000  avgt   30    2.456 ± 0.123  μs/op
AccountFinderJMHBenchmark.parallelStream              1000  avgt   30    1.234 ± 0.067  μs/op
AccountFinderJMHBenchmark.concurrent                   1000  avgt   30    1.456 ± 0.089  μs/op
AccountFinderJMHBenchmark.optimized                    1000  avgt   30    1.123 ± 0.045  μs/op
```

## Performance Analysis

### Expected Results

1. **Small datasets (1,000 accounts)**: Parallel overhead may make single-threaded faster
2. **Medium datasets (10,000 accounts)**: Parallel implementations should show improvement
3. **Large datasets (100,000 accounts)**: Significant performance gains with parallel processing

### Factors Affecting Performance

- **Dataset size**: Larger datasets benefit more from parallelization
- **CPU cores**: More cores = better parallel performance
- **JVM optimizations**: JMH handles JIT warmup properly
- **Memory access patterns**: Cache-friendly access improves performance

## Customizing Benchmarks

### Modify Parameters

Edit the `@Param` annotation in `AccountFinderJMHBenchmark.java`:

```java
@Param({"1000", "10000", "100000", "1000000"})
private int accountCount;
```

### Adjust Iterations

```java
@Warmup(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 20, time = 2, timeUnit = TimeUnit.SECONDS)
```

### Change Thread Count

```java
@Threads(4) // Use 4 threads
```

## Troubleshooting

### Common Issues

1. **OutOfMemoryError**: Increase heap size with `-Xmx4g`
2. **Slow execution**: Reduce iterations or fork count
3. **Inconsistent results**: Increase warmup iterations

### JVM Options

```bash
java -Xmx4g -XX:+UseG1GC -jar target/benchmarks.jar
```

## Best Practices

1. **Run on dedicated hardware**: Avoid other processes
2. **Use consistent environment**: Same JVM, OS, hardware
3. **Multiple runs**: Run benchmarks multiple times
4. **Statistical significance**: Use sufficient iterations
5. **Profile first**: Use profilers to understand bottlenecks

## Results Interpretation

- **Lower is better** for time-based metrics
- **Higher is better** for throughput metrics
- **Statistical significance**: Look at error margins
- **Consistency**: Results should be reproducible across runs
