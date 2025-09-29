package manning.performance.wordservice.traced;


import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Traced version of WordsController with performance metrics collection
 * to identify hot paths and bottlenecks in the word-of-the-day application.
 */
@Path("/traced-words")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TracedWordsController {
    
    private final TracedWordsService wordsService;
    private final MetricsCollector metrics;

    public TracedWordsController() {
        java.nio.file.Path defaultPath = getPath("words.txt");
        wordsService = new TracedWordsService(defaultPath);
        metrics = MetricsCollector.getInstance();
    }

    /**
     * Word of the day endpoint with performance tracing.
     * Called once per day according to simulation.
     */
    @GET
    @Path("/word-of-the-day")
    public Response getWordOfTheDay() {
        long startTime = System.nanoTime();
        
        try {
            String word = wordsService.getWordOfTheDay();
            
            // Record request-level metrics
            metrics.recordMethodExecution("getWordOfTheDay_endpoint", System.nanoTime() - startTime);
            
            return Response.ok(word).build();
        } catch (Exception e) {
            metrics.recordMethodExecution("getWordOfTheDay_endpoint", System.nanoTime() - startTime);
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving word of the day: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Word validation endpoint with performance tracing.
     * This is the HOT PATH - called 20 times per second according to simulation.
     */
    @GET
    @Path("/word-exists")
    public Response validateWord(@QueryParam("word") String word) {
        long startTime = System.nanoTime();
        
        try {
            boolean exists = wordsService.wordExists(word);
            
            // Record request-level metrics
            metrics.recordMethodExecution("wordExists_endpoint", System.nanoTime() - startTime);
            
            return Response.status(Status.OK.getStatusCode(), String.valueOf(exists)).build();
        } catch (Exception e) {
            metrics.recordMethodExecution("wordExists_endpoint", System.nanoTime() - startTime);
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Error validating word: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Metrics endpoint to retrieve performance data
     */
    @GET
    @Path("/metrics")
    public Response getMetrics() {
        long startTime = System.nanoTime();
        
        try {
            MetricsCollector.MetricsReport report = wordsService.getMetrics();
            
            metrics.recordMethodExecution("getMetrics_endpoint", System.nanoTime() - startTime);
            
            return Response.ok(report).build();
        } catch (Exception e) {
            metrics.recordMethodExecution("getMetrics_endpoint", System.nanoTime() - startTime);
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving metrics: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Reset metrics endpoint
     */
    @GET
    @Path("/reset-metrics")
    public Response resetMetrics() {
        long startTime = System.nanoTime();
        
        try {
            wordsService.resetMetrics();
            
            metrics.recordMethodExecution("resetMetrics_endpoint", System.nanoTime() - startTime);
            
            return Response.ok("Metrics reset successfully").build();
        } catch (Exception e) {
            metrics.recordMethodExecution("resetMetrics_endpoint", System.nanoTime() - startTime);
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Error resetting metrics: " + e.getMessage())
                    .build();
        }
    }

    private java.nio.file.Path getPath(String filename) {
        try {
            return Paths.get(
                    Objects.requireNonNull(getClass().getClassLoader().getResource(filename)).toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid " + filename + " path", e);
        }
    }
}
