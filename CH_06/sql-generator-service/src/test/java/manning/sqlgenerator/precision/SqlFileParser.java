package manning.sqlgenerator.precision;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing SQL files and extracting table definitions.
 */
public class SqlFileParser {

    private static final Pattern CREATE_TABLE_PATTERN = Pattern.compile(
        "CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?[\"`]?([\\w_]+)[\"`]?\\s*\\((.*?)\\)\\s*;?",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    /**
     * Parses a SQL file and extracts table definitions.
     *
     * @param sqlFileName The name of the SQL file in the resources directory
     * @return Map of table name to CREATE TABLE SQL statement
     * @throws IOException If the file cannot be read
     */
    public static Map<String, String> parseSqlFile(String sqlFileName) throws IOException {
        Map<String, String> tableDefinitions = new HashMap<>();
        
        try (InputStream inputStream = SqlFileParser.class.getClassLoader()
                .getResourceAsStream(sqlFileName)) {
            
            if (inputStream == null) {
                throw new IOException("SQL file not found: " + sqlFileName);
            }
            
            String sqlContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            
            // Find all CREATE TABLE statements
            Matcher matcher = CREATE_TABLE_PATTERN.matcher(sqlContent);
            
            while (matcher.find()) {
                String tableName = matcher.group(1);
                String tableDefinition = matcher.group(0); // Full CREATE TABLE statement
                
                tableDefinitions.put(tableName, tableDefinition);
            }
        }
        
        return tableDefinitions;
    }
}
