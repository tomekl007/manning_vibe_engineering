package manning.sqlgenerator.precision;

import java.util.*;
import java.util.stream.Collectors;

import static manning.sqlgenerator.precision.ValidateSqlGeneratorPrecision.*;

/**
 * Extracts recent queries from dev.json for benchmarking context.
 * Uses the existing DbInput class and provides the last N queries for a specific database.
 */
public class DevJsonQueryExtractor {
    
    private static List<DbInput> allEntries = null;
    
    /**
     * Sets the loaded DbInput entries from the main benchmark class.
     * This should be called after loadDbInput() in ValidateSqlGeneratorPrecision.
     * 
     * @param dbInputs List of DbInput objects loaded from dev.json
     */
    public static void setDbInputs(List<DbInput> dbInputs) {
        allEntries = dbInputs;
    }
    
    /**
     * Gets the last N queries for a specific database.
     * 
     * @param dbId Database identifier
     * @param n Number of recent queries to return
     * @return List of recent SQL queries (most recent last)
     */
    public static List<String> getLastNQueries(String dbId, int n) {
        if (allEntries == null || allEntries.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> filteredQueries = allEntries.stream()
            .filter(entry -> dbId.equals(entry.getDbId()))
            .map(DbInput::getSql)
            .filter(sql -> sql != null && !sql.trim().isEmpty())
            .collect(Collectors.toList());
        
        // Debug logging
        System.out.println("Debug: dbId=" + dbId + ", requested=" + n + ", filtered=" + filteredQueries.size());
        
        // Get the last N queries from the filtered results
        int startIndex = Math.max(0, filteredQueries.size() - n);
        int endIndex = filteredQueries.size();
        
        System.out.println("Debug: startIndex=" + startIndex + ", endIndex=" + endIndex);
        
        return filteredQueries.subList(startIndex, endIndex);
    }
    
    /**
     * Gets the last N queries for a specific database (default: 2).
     * 
     * @param dbId Database identifier
     * @return List of recent SQL queries
     */
    public static List<String> getLastNQueries(String dbId) {
        return getLastNQueries(dbId, NR_OF_LAST_QUERIES_INCLUDED);
    }

}
