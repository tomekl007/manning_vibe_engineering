package manning.sqlgenerator.precision;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for mapping the dataset IDs used in tests to their corresponding table names.
 */
public final class DatasetToTablesConfig {

    private DatasetToTablesConfig() {}

    public static final Map<String, List<String>> DATASET_CONFIG_PER_DB_ID = new HashMap<>();

    static {
        DATASET_CONFIG_PER_DB_ID.put(
            "california_schools",
            Arrays.asList("frpm", "satscores", "schools"));

        DATASET_CONFIG_PER_DB_ID.put(
            "debit_card_specializing",
            Arrays.asList("customers", "gasstations", "products", "yearmonth", "transactions_1k"));

        DATASET_CONFIG_PER_DB_ID.put(
            "thrombosis_prediction",
            Arrays.asList("Examination", "Patient", "Laboratory", "directors", "movies"));
    }
}
