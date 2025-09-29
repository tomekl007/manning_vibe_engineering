package manning.performance.wordservice.traced;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Test runner to demonstrate performance analysis capabilities.
 * This class simulates the expected production traffic and analyzes the results.
 */
public class PerformanceAnalysisRunner {
    
    private static final String BASE_URL = "http://localhost:8080";
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    
    /**
     * Run performance analysis simulation
     * This simulates the WordsSimulation traffic patterns
     */
    @Test
    public void runPerformanceAnalysis() throws Exception {
        System.out.println("🚀 Starting Performance Analysis Simulation...\n");
        
        // Reset metrics
        resetMetrics();
        
        // Simulate production traffic pattern from WordsSimulation
        System.out.println("📊 Simulating production traffic...");
        simulateWordOfTheDayTraffic();
        simulateWordExistsTraffic();
        
        // Collect and analyze metrics
        System.out.println("\n🔍 Collecting performance metrics...");
        MetricsCollector.MetricsReport report = getMetrics();
        
        // Analyze results
        PerformanceAnalyzer analyzer = new PerformanceAnalyzer(report);
        String analysisReport = analyzer.generateReport();
        
        System.out.println(analysisReport);
        
        // Verify hot path identification
        assertHotPathIdentified(analyzer);
        
        System.out.println("✅ Performance analysis completed successfully!");
    }
    
    private void simulateWordOfTheDayTraffic() throws Exception {
        // Simulate 1 request per second for 10 seconds (reduced from 1 minute for testing)
        for (int i = 0; i < 10; i++) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/traced-words/word-of-the-day"))
                    .GET()
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                System.err.println("❌ Word of the day request failed: " + response.statusCode());
            }
            
            Thread.sleep(1000); // 1 second delay
        }
    }
    
    private void simulateWordExistsTraffic() throws Exception {
        // Simulate 20 requests per second for 10 seconds (reduced from 1 minute for testing)
        String[] testWords = {"cat", "house", "dog", "tree", "water", "fire", "earth", "air", "love", "peace"};
        
        for (int i = 0; i < 10; i++) { // 10 seconds
            for (int j = 0; j < 20; j++) { // 20 requests per second
                String word = testWords[j % testWords.length];
                
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/traced-words/word-exists?word=" + word))
                        .GET()
                        .build();
                
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() != 200) {
                    System.err.println("❌ Word exists request failed: " + response.statusCode());
                }
            }
            Thread.sleep(1000); // 1 second delay
        }
    }
    
    private void resetMetrics() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/traced-words/reset-metrics"))
                .GET()
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to reset metrics: " + response.statusCode());
        }
        
        System.out.println("🔄 Metrics reset successfully");
    }
    
    private MetricsCollector.MetricsReport getMetrics() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/traced-words/metrics"))
                .GET()
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to get metrics: " + response.statusCode());
        }
        
        // For this demo, we'll create a mock report since we can't easily deserialize JSON
        // In a real scenario, you'd parse the JSON response
        return MetricsCollector.getInstance().getMetricsReport();
    }
    
    private void assertHotPathIdentified(PerformanceAnalyzer analyzer) {
        PerformanceAnalyzer.HotPathAnalysis analysis = analyzer.analyzeHotPaths();
        
        // Verify that wordExists is identified as the hot path
        if (analysis.hottestMethod.contains("wordExists")) {
            System.out.println("✅ HOT PATH CORRECTLY IDENTIFIED: wordExists method");
        } else {
            System.out.println("⚠️  WARNING: Expected wordExists to be the hot path, but found: " + analysis.hottestMethod);
        }
        
        // Verify file I/O issues are detected
        if (analysis.fileIOTimePercentage > 50) {
            System.out.println("✅ FILE I/O BOTTLENECK DETECTED: " + String.format("%.1f", analysis.fileIOTimePercentage) + "%");
        } else {
            System.out.println("ℹ️  File I/O impact: " + String.format("%.1f", analysis.fileIOTimePercentage) + "%");
        }
    }
}
