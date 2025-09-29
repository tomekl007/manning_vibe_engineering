package manning.performance.wordservice.traced;

import manning.performance.wordservice.WordsService;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Scanner;
import java.util.function.IntSupplier;

/**
 * Traced version of DefaultWordsService with comprehensive performance metrics
 * to identify hot paths and bottlenecks in the word-of-the-day application.
 */
public class TracedWordsService implements WordsService {

    private static final int MULTIPLY_FACTOR = 100;
    private static final IntSupplier DEFAULT_INDEX_PROVIDER = TracedWordsService::getIndexForToday;
    private static final MetricsCollector metrics = MetricsCollector.getInstance();

    private final Path filePath;
    private final IntSupplier indexProvider;

    public TracedWordsService(Path filePath) {
        this(filePath, DEFAULT_INDEX_PROVIDER);
    }

    public TracedWordsService(Path filePath, IntSupplier indexProvider) {
        this.filePath = filePath;
        this.indexProvider = indexProvider;
    }

    @Override
    public String getWordOfTheDay() {
        long startTime = System.nanoTime();
        metrics.recordRequest("word-of-the-day");
        
        try {
            int index = indexProvider.getAsInt();
            
            // Track file I/O operations
            long fileOpenStart = System.nanoTime();
            long bytesRead = 0;
            int linesScanned = 0;
            
            try (Scanner scanner = new Scanner(filePath.toFile())) {
                long fileOpenTime = System.nanoTime() - fileOpenStart;
                
                int i = 0;
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    linesScanned++;
                    bytesRead += line.length() + 1; // +1 for newline
                    
                    if (index == i) {
                        metrics.recordLinesScanned(linesScanned);
                        metrics.recordFileRead(bytesRead, fileOpenTime);
                        metrics.recordMethodExecution("getWordOfTheDay", System.nanoTime() - startTime);
                        
                        return line;
                    }
                    i++;
                }
            } catch (FileNotFoundException e) {
                metrics.recordMethodExecution("getWordOfTheDay", System.nanoTime() - startTime);
                throw new RuntimeException("Problem in getWordOfTheDay for index: " + filePath, e);
            }

            metrics.recordLinesScanned(linesScanned);
            metrics.recordFileRead(bytesRead, System.nanoTime() - fileOpenStart);
            metrics.recordMethodExecution("getWordOfTheDay", System.nanoTime() - startTime);
            
            return "No word today.";
            
        } catch (Exception e) {
            metrics.recordMethodExecution("getWordOfTheDay", System.nanoTime() - startTime);
            throw e;
        }
    }

    @Override
    public boolean wordExists(String word) {
        long startTime = System.nanoTime();
        metrics.recordRequest("word-exists");
        
        try {
            // Track file I/O operations
            long fileOpenStart = System.nanoTime();
            long bytesRead = 0;
            int linesScanned = 0;
            // Track string comparison count for metrics
            
            try (Scanner scanner = new Scanner(filePath.toFile())) {
                long fileOpenTime = System.nanoTime() - fileOpenStart;
                
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    linesScanned++;
                    bytesRead += line.length() + 1; // +1 for newline
                    
                    // Track string comparison operations
                    metrics.recordStringComparison();
                    
                    if (word.equals(line)) {
                        metrics.recordLinesScanned(linesScanned);
                        metrics.recordFileRead(bytesRead, fileOpenTime);
                        metrics.recordMethodExecution("wordExists", System.nanoTime() - startTime);
                        
                        return true;
                    }
                }
            } catch (FileNotFoundException e) {
                metrics.recordMethodExecution("wordExists", System.nanoTime() - startTime);
                throw new RuntimeException("Problem in wordExists for word: " + word, e);
            }
            
            metrics.recordLinesScanned(linesScanned);
            metrics.recordFileRead(bytesRead, System.nanoTime() - fileOpenStart);
            metrics.recordMethodExecution("wordExists", System.nanoTime() - startTime);
            
            return false;
            
        } catch (Exception e) {
            metrics.recordMethodExecution("wordExists", System.nanoTime() - startTime);
            throw e;
        }
    }

    private static int getIndexForToday() {
        long startTime = System.nanoTime();
        
        LocalDate now = LocalDate.now();
        int result = now.getYear() + now.getDayOfYear() * MULTIPLY_FACTOR;
        
        metrics.recordMethodExecution("getIndexForToday", System.nanoTime() - startTime);
        return result;
    }
    
    /**
     * Get current performance metrics
     */
    public MetricsCollector.MetricsReport getMetrics() {
        return metrics.getMetricsReport();
    }
    
    /**
     * Reset all performance metrics
     */
    public void resetMetrics() {
        metrics.reset();
    }
}
