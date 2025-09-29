package manning.performance.wordservice.traced;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class to analyze performance metrics and identify hot paths
 * and optimization opportunities in the word-of-the-day application.
 */
public class PerformanceAnalyzer {
    
    private final MetricsCollector.MetricsReport report;
    
    public PerformanceAnalyzer(MetricsCollector.MetricsReport report) {
        this.report = report;
    }
    
    /**
     * Analyze and identify the hottest paths in the application
     */
    public HotPathAnalysis analyzeHotPaths() {
        // Find methods with highest total execution time
        Map<String, Double> methodImpact = report.averageMethodTimesMs.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue() * report.methodCallCounts.get(entry.getKey())
            ));
        
        // Find the method with highest impact
        String hottestMethod = methodImpact.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("unknown");
        
        // Calculate efficiency metrics
        double totalRequestTime = report.averageMethodTimesMs.values().stream()
            .mapToDouble(Double::doubleValue)
            .sum();
        
        double fileIOTimePercentage = (report.totalFileOpenTimeMs / totalRequestTime) * 100;
        double stringOpsTimePercentage = estimateStringOpsTimePercentage();
        
        return new HotPathAnalysis(
            hottestMethod,
            methodImpact,
            fileIOTimePercentage,
            stringOpsTimePercentage,
            calculateOptimizationRecommendations()
        );
    }
    
    /**
     * Estimate the percentage of time spent on string operations
     * Based on the number of string comparisons relative to total operations
     */
    private double estimateStringOpsTimePercentage() {
        if (report.totalRequests == 0) return 0;
        
        // Rough estimate: each string comparison takes ~1-10 nanoseconds
        // This is a heuristic based on typical string comparison performance
        long estimatedStringOpsTime = report.totalStringComparisons * 5; // 5ns per comparison
        double totalEstimatedTime = report.totalRequests * 1_000_000; // 1ms per request estimate
        
        return (estimatedStringOpsTime / totalEstimatedTime) * 100;
    }
    
    /**
     * Calculate optimization recommendations based on metrics
     */
    private String calculateOptimizationRecommendations() {
        StringBuilder recommendations = new StringBuilder();
        
        // File I/O analysis
        if (report.totalFileReads > report.totalRequests) {
            recommendations.append("🔴 CRITICAL: File is being read multiple times per request!\n");
            recommendations.append("   Recommendation: Cache the dictionary in memory (HashMap/HashSet)\n");
            recommendations.append("   Expected improvement: 90-99% reduction in response time\n\n");
        }
        
        // String operations analysis
        if (report.totalStringComparisons > report.totalLinesScanned * 0.8) {
            recommendations.append("🟡 MODERATE: High number of string comparisons detected\n");
            recommendations.append("   Recommendation: Use HashSet for O(1) lookups instead of linear search\n");
            recommendations.append("   Expected improvement: 95-99% reduction in lookup time\n\n");
        }
        
        // Memory usage analysis
        if (report.totalMemoryUsed > 0) {
            recommendations.append("📊 MEMORY: Consider memory usage patterns\n");
            recommendations.append("   Current memory usage: ").append(report.totalMemoryUsed).append(" bytes\n");
            recommendations.append("   Recommendation: Monitor memory usage with caching solution\n\n");
        }
        
        // Endpoint analysis
        if (report.endpointCallCounts.containsKey("word-exists") && 
            report.endpointCallCounts.get("word-exists") > report.endpointCallCounts.getOrDefault("word-of-the-day", 0L) * 10) {
            recommendations.append("🎯 HOT PATH IDENTIFIED: /word-exists endpoint\n");
            recommendations.append("   Traffic ratio: ").append(report.endpointCallCounts.get("word-exists")).append(" vs ")
                         .append(report.endpointCallCounts.getOrDefault("word-of-the-day", 0L)).append(" requests\n");
            recommendations.append("   Priority: HIGH - This endpoint needs immediate optimization\n\n");
        }
        
        if (recommendations.length() == 0) {
            recommendations.append("✅ No critical performance issues detected.\n");
        }
        
        return recommendations.toString();
    }
    
    /**
     * Generate a comprehensive performance report
     */
    public String generateReport() {
        HotPathAnalysis analysis = analyzeHotPaths();
        
        StringBuilder report = new StringBuilder();
        report.append("=".repeat(80)).append("\n");
        report.append("🔍 PERFORMANCE ANALYSIS REPORT\n");
        report.append("=".repeat(80)).append("\n\n");
        
        report.append("📈 HOT PATH ANALYSIS\n");
        report.append("-".repeat(40)).append("\n");
        report.append("Hottest method: ").append(analysis.hottestMethod).append("\n");
        report.append("File I/O time percentage: ").append(String.format("%.1f", analysis.fileIOTimePercentage)).append("%\n");
        report.append("String operations time percentage: ").append(String.format("%.1f", analysis.stringOpsTimePercentage)).append("%\n\n");
        
        report.append("📊 METHOD IMPACT ANALYSIS\n");
        report.append("-".repeat(40)).append("\n");
        analysis.methodImpact.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .forEach(entry -> 
                report.append(String.format("%-30s: %8.2f ms total impact\n", 
                    entry.getKey(), entry.getValue())));
        report.append("\n");
        
        report.append("🚀 OPTIMIZATION RECOMMENDATIONS\n");
        report.append("-".repeat(40)).append("\n");
        report.append(analysis.recommendations);
        
        report.append("📋 DETAILED METRICS\n");
        report.append("-".repeat(40)).append("\n");
        report.append(this.report.toString());
        
        return report.toString();
    }
    
    /**
     * Data class for hot path analysis results
     */
    public static class HotPathAnalysis {
        public final String hottestMethod;
        public final Map<String, Double> methodImpact;
        public final double fileIOTimePercentage;
        public final double stringOpsTimePercentage;
        public final String recommendations;
        
        public HotPathAnalysis(String hottestMethod,
                             Map<String, Double> methodImpact,
                             double fileIOTimePercentage,
                             double stringOpsTimePercentage,
                             String recommendations) {
            this.hottestMethod = hottestMethod;
            this.methodImpact = methodImpact;
            this.fileIOTimePercentage = fileIOTimePercentage;
            this.stringOpsTimePercentage = stringOpsTimePercentage;
            this.recommendations = recommendations;
        }
    }
}
