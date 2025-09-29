package manning.performance.wordservice.traced;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.Map;

/**
 * Thread-safe metrics collector for tracking performance metrics
 * at granular level to identify hot paths and bottlenecks.
 */
public class MetricsCollector {
    
    private static final MetricsCollector INSTANCE = new MetricsCollector();
    
    // Method execution times (in nanoseconds)
    private final Map<String, LongAdder> methodExecutionTimes = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> methodCallCounts = new ConcurrentHashMap<>();
    
    // File I/O metrics
    private final LongAdder totalFileReads = new LongAdder();
    private final LongAdder totalBytesRead = new LongAdder();
    private final LongAdder totalFileOpenTime = new LongAdder();
    
    // String operation metrics
    private final LongAdder totalStringComparisons = new LongAdder();
    private final LongAdder totalLinesScanned = new LongAdder();
    
    // Request-level metrics
    private final LongAdder totalRequests = new LongAdder();
    private final Map<String, LongAdder> endpointMetrics = new ConcurrentHashMap<>();
    
    // Memory metrics
    private final LongAdder totalMemoryUsed = new LongAdder();
    
    private MetricsCollector() {}
    
    public static MetricsCollector getInstance() {
        return INSTANCE;
    }
    
    /**
     * Record method execution time
     */
    public void recordMethodExecution(String methodName, long executionTimeNanos) {
        methodExecutionTimes.computeIfAbsent(methodName, k -> new LongAdder()).add(executionTimeNanos);
        methodCallCounts.computeIfAbsent(methodName, k -> new AtomicLong()).incrementAndGet();
    }
    
    /**
     * Record file I/O operations
     */
    public void recordFileRead(long bytesRead, long openTimeNanos) {
        totalFileReads.increment();
        totalBytesRead.add(bytesRead);
        totalFileOpenTime.add(openTimeNanos);
    }
    
    /**
     * Record string operations
     */
    public void recordStringComparison() {
        totalStringComparisons.increment();
    }
    
    public void recordLinesScanned(long linesCount) {
        totalLinesScanned.add(linesCount);
    }
    
    /**
     * Record request-level metrics
     */
    public void recordRequest(String endpoint) {
        totalRequests.increment();
        endpointMetrics.computeIfAbsent(endpoint, k -> new LongAdder()).increment();
    }
    
    /**
     * Record memory usage
     */
    public void recordMemoryUsage(long bytesUsed) {
        totalMemoryUsed.add(bytesUsed);
    }
    
    /**
     * Get comprehensive metrics report
     */
    public MetricsReport getMetricsReport() {
        Map<String, Double> avgMethodTimes = new ConcurrentHashMap<>();
        methodExecutionTimes.forEach((method, totalTime) -> {
            long callCount = methodCallCounts.get(method).get();
            avgMethodTimes.put(method, (double) totalTime.sum() / callCount / 1_000_000.0); // Convert to milliseconds
        });
        
        return new MetricsReport(
            avgMethodTimes,
            methodCallCounts.entrySet().stream()
                .collect(ConcurrentHashMap::new, 
                    (map, entry) -> map.put(entry.getKey(), entry.getValue().get()), 
                    ConcurrentHashMap::putAll),
            totalFileReads.sum(),
            totalBytesRead.sum(),
            totalFileOpenTime.sum() / 1_000_000.0, // Convert to milliseconds
            totalStringComparisons.sum(),
            totalLinesScanned.sum(),
            totalRequests.sum(),
            endpointMetrics.entrySet().stream()
                .collect(ConcurrentHashMap::new, 
                    (map, entry) -> map.put(entry.getKey(), entry.getValue().sum()), 
                    ConcurrentHashMap::putAll),
            totalMemoryUsed.sum()
        );
    }
    
    /**
     * Reset all metrics
     */
    public void reset() {
        methodExecutionTimes.clear();
        methodCallCounts.clear();
        totalFileReads.reset();
        totalBytesRead.reset();
        totalFileOpenTime.reset();
        totalStringComparisons.reset();
        totalLinesScanned.reset();
        totalRequests.reset();
        endpointMetrics.clear();
        totalMemoryUsed.reset();
    }
    
    /**
     * Metrics report data class
     */
    public static class MetricsReport {
        public final Map<String, Double> averageMethodTimesMs;
        public final Map<String, Long> methodCallCounts;
        public final long totalFileReads;
        public final long totalBytesRead;
        public final double totalFileOpenTimeMs;
        public final long totalStringComparisons;
        public final long totalLinesScanned;
        public final long totalRequests;
        public final Map<String, Long> endpointCallCounts;
        public final long totalMemoryUsed;
        
        public MetricsReport(Map<String, Double> averageMethodTimesMs,
                           Map<String, Long> methodCallCounts,
                           long totalFileReads,
                           long totalBytesRead,
                           double totalFileOpenTimeMs,
                           long totalStringComparisons,
                           long totalLinesScanned,
                           long totalRequests,
                           Map<String, Long> endpointCallCounts,
                           long totalMemoryUsed) {
            this.averageMethodTimesMs = averageMethodTimesMs;
            this.methodCallCounts = methodCallCounts;
            this.totalFileReads = totalFileReads;
            this.totalBytesRead = totalBytesRead;
            this.totalFileOpenTimeMs = totalFileOpenTimeMs;
            this.totalStringComparisons = totalStringComparisons;
            this.totalLinesScanned = totalLinesScanned;
            this.totalRequests = totalRequests;
            this.endpointCallCounts = endpointCallCounts;
            this.totalMemoryUsed = totalMemoryUsed;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== PERFORMANCE METRICS REPORT ===\n");
            sb.append("\n--- METHOD PERFORMANCE ---\n");
            averageMethodTimesMs.forEach((method, avgTime) -> 
                sb.append(String.format("%-30s: %8.2f ms (called %d times)\n", 
                    method, avgTime, methodCallCounts.get(method))));
            
            sb.append("\n--- FILE I/O METRICS ---\n");
            sb.append(String.format("Total file reads: %d\n", totalFileReads));
            sb.append(String.format("Total bytes read: %,d\n", totalBytesRead));
            sb.append(String.format("Total file open time: %.2f ms\n", totalFileOpenTimeMs));
            
            sb.append("\n--- STRING OPERATIONS ---\n");
            sb.append(String.format("Total string comparisons: %,d\n", totalStringComparisons));
            sb.append(String.format("Total lines scanned: %,d\n", totalLinesScanned));
            
            sb.append("\n--- REQUEST METRICS ---\n");
            sb.append(String.format("Total requests: %d\n", totalRequests));
            endpointCallCounts.forEach((endpoint, count) -> 
                sb.append(String.format("%-20s: %d requests\n", endpoint, count)));
            
            sb.append("\n--- MEMORY USAGE ---\n");
            sb.append(String.format("Total memory used: %,d bytes\n", totalMemoryUsed));
            
            return sb.toString();
        }
    }
}
