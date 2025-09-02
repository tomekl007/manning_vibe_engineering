package manning.sqlgenerator.precision;

import manning.sqlgenerator.dto.Table;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for mapping the dataset IDs used in tests to their corresponding table definitions.
 * Now includes complete table structure from SQL files.
 */
public final class DatasetToTablesConfig {

    private DatasetToTablesConfig() {}

    // Map from dataset ID to SQL file name
    private static final Map<String, String> DATASET_SQL_FILES = new HashMap<>();
    
    // Map from dataset ID to list of table names
    private static final Map<String, List<String>> DATASET_TABLE_NAMES = new HashMap<>();
    
    // Map from dataset ID to complete table definitions
    static final Map<String, List<Table>> DATASET_CONFIG_PER_DB_ID = new HashMap<>();

    static {
        // Initialize dataset to SQL file mappings
        DATASET_SQL_FILES.put("california_schools", "california_schools.sql");
        DATASET_SQL_FILES.put("debit_card_specializing", "debit_card_specializing.sql");
        DATASET_SQL_FILES.put("thrombosis_prediction", "thrombosis_prediction.sql");

        // Initialize dataset to table names mappings
        DATASET_TABLE_NAMES.put("california_schools", Arrays.asList("frpm", "satscores", "schools"));
        DATASET_TABLE_NAMES.put("debit_card_specializing", Arrays.asList("customers", "gasstations", "products", "yearmonth", "transactions_1k"));
        DATASET_TABLE_NAMES.put("thrombosis_prediction", Arrays.asList("Examination", "Patient", "Laboratory", "directors", "movies"));

        // Load complete table definitions from SQL files
        loadTableDefinitions();
    }

    /**
     * Loads complete table definitions from SQL files for all datasets.
     */
    private static void loadTableDefinitions() {
        for (Map.Entry<String, String> entry : DATASET_SQL_FILES.entrySet()) {
            String datasetId = entry.getKey();
            String sqlFileName = entry.getValue();
            
            try {
                Map<String, String> tableDefinitions = SqlFileParser.parseSqlFile(sqlFileName);
                List<String> tableNames = DATASET_TABLE_NAMES.get(datasetId);
                
                List<Table> tables = tableNames.stream()
                    .map(tableName -> {
                        String createTableSql = tableDefinitions.get(tableName);
                        if (createTableSql != null) {
                            return new Table(tableName, createTableSql);
                        } else {
                            // Fallback to basic table if SQL not found
                            return new Table(tableName);
                        }
                    })
                    .toList();
                
                DATASET_CONFIG_PER_DB_ID.put(datasetId, tables);
                
            } catch (IOException e) {
                System.err.println("Warning: Could not load SQL definitions for dataset " + datasetId + ": " + e.getMessage());
                // Fallback to basic table names only
                List<Table> fallbackTables = DATASET_TABLE_NAMES.get(datasetId).stream()
                    .map(Table::new)
                    .toList();
                DATASET_CONFIG_PER_DB_ID.put(datasetId, fallbackTables);
            }
        }
    }

    /**
     * Gets the complete table definitions for a dataset.
     *
     * @param datasetId The dataset identifier
     * @return List of Table objects with complete structure information
     */
    public static List<Table> getTableDefinitions(String datasetId) {
        return DATASET_CONFIG_PER_DB_ID.get(datasetId);
    }

    /**
     * Gets just the table names for a dataset (backward compatibility).
     *
     * @param datasetId The dataset identifier
     * @return List of table names
     */
    public static List<String> getTableNames(String datasetId) {
        return DATASET_TABLE_NAMES.get(datasetId);
    }
}
