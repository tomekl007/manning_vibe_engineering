package manning.sqlgenerator.precision;

import static manning.sqlgenerator.precision.DatasetToTablesConfig.DATASET_CONFIG_PER_DB_ID;
import static manning.sqlgenerator.precision.TestRestClient.generateSql;
import static manning.sqlgenerator.precision.TestRestClient.isServiceHealthy;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import manning.sqlgenerator.dto.SqlGeneratorQueryResponse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * E2E precision test for the SQL Generator Service using REST API.
 *
 * To run this test you need the following pre-requisites:
 * <ul>
 *   <li>SQL Generator Service running on localhost:8080 (or set BASE_URL environment variable)</li>
 *   <li>Valid OpenAI API key configured in the service</li>
 *   <li>Test data file (dev.json) in the test resources</li>
 * </ul>
 *
 * This test replaces the original gRPC-based ValidateAutoGenerateQueryPrecision
 * with a REST API-based approach for the new sql-generator-service.
 */
public class ValidateSqlGeneratorPrecision {

    private static final String DEFAULT_BASE_URL = "http://localhost:8080";

    /**
     * Main method to run the precision validation test.
     */
    public static void main(String[] args) throws IOException {
        // Get base URL from environment or use default
        String baseUrl = System.getenv("BASE_URL");
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            baseUrl = DEFAULT_BASE_URL;
        }

        System.out.println("Using SQL Generator Service at: " + baseUrl);

        // Check if service is healthy
        if (!isServiceHealthy(baseUrl)) {
            System.err.println("SQL Generator Service is not healthy at " + baseUrl);
            System.err.println("Please ensure the service is running and accessible");
            System.exit(1);
        }

        System.out.println("Service health check passed");

        // Load test data
        List<DbInput> dbInputs = loadDbInput();

        // Filter inputs to only include those with available dataset configs
        List<DbInput> inputSubset = dbInputs.stream()
            .filter(extractDBs())
            .limit(10)
            .collect(Collectors.toList());

        System.out.println("Will run the SQL generation for " + inputSubset.size() + " number of inputs.");

        // Create output file
        File tempDirectory = new File(System.getProperty("java.io.tmpdir"));
        Path filePath = Files.createTempFile(
            tempDirectory.toPath(),
            "sql_generator_precision_result_",
            "_" + Instant.now().toEpochMilli() + ".csv"
        );

        System.out.println("The results will be saved to: " + filePath);

        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath.toString()))) {
            // Write the header
            String[] header = {
                "db_id",
                "difficulty",
                "question",
                "expected",
                "actual",
                "levenshtein_similarity",
                "overlap_similarity"
            };
            writer.writeNext(header);

            // Process each input
          String finalBaseUrl = baseUrl;
          inputSubset.stream()
                .map(input -> processDbInput(input, finalBaseUrl))
                .forEach(result -> showResultAndSave(result, writer));
        }

        System.out.println("Precision validation completed. Results saved to: " + filePath);
    }

    /**
     * Filters database inputs to only include those with available dataset configurations.
     */
    private static Predicate<DbInput> extractDBs() {
        return input -> DATASET_CONFIG_PER_DB_ID.containsKey(input.getDbId());
    }

    /**
     * Processes a single database input and generates SQL.
     */
    private static SqlGenerationResult processDbInput(DbInput input, String baseUrl) {
        System.out.println("Processing: " + input.getDbId() + " - " + input.getQuestion());

        // Get the dataset configurations for this database
        List<String> tableNames = DATASET_CONFIG_PER_DB_ID.get(input.getDbId());
        if (tableNames == null) {
            System.out.println("No dataset configuration found for: " + input.getDbId());
            return new SqlGenerationResult(null, input);
        }

        // Call the REST API to generate SQL
        SqlGeneratorQueryResponse response = generateSql(
            tableNames, baseUrl, input.getQuestion());

        return new SqlGenerationResult(response, input);
    }

    /**
     * Shows the result and saves it to CSV.
     */
    private static void showResultAndSave(SqlGenerationResult result, CSVWriter writer) {
        System.out.println("Question: " + result.getDbInput().getQuestion());
        System.out.println("Expected result: " + result.getDbInput().getSql());
        System.out.println("Actual result: " + result.getActualSql());

        // Normalize the generated SQL for comparison
        String normalized = QueryNormalizer.normalize(result.getActualSql(), null);

        // Calculate similarity scores
        double levenshteinSimilarity = LevenshteinSimilarityCalculator.calculateQuerySimilarity(
            result.getDbInput().getSql(), normalized);
        double overlapSimilarity = OverlapSimilarityCalculator.calculateQuerySimilarity(
            result.getDbInput().getSql(), normalized);

        // Write to CSV
        String[] dataRow = {
            result.getDbInput().getDbId(),
            result.getDbInput().getDifficulty(),
            result.getDbInput().getQuestion(),
            result.getDbInput().getSql(),
            normalized,
            String.valueOf(levenshteinSimilarity),
            String.valueOf(overlapSimilarity)
        };

        writer.writeNext(dataRow);

        // Print similarity scores
        System.out.println("Levenshtein similarity: " + levenshteinSimilarity);
        System.out.println("Overlap similarity: " + overlapSimilarity);
        System.out.println("---");
    }

    /**
     * Result of a SQL generation test.
     */
    static class SqlGenerationResult {
        private final String actualSql;
        private final DbInput dbInput;

        SqlGenerationResult(SqlGeneratorQueryResponse response, DbInput dbInput) {
            this.actualSql = response != null && response.isSuccess() ? response.getSql() : "ERROR: " + response.getMessage();
            this.dbInput = dbInput;
        }

        public String getActualSql() {
            return actualSql;
        }

        public DbInput getDbInput() {
            return dbInput;
        }
    }

    /**
     * Loads test data from the dev.json file.
     */
    private static List<DbInput> loadDbInput() throws IOException {
        String resourceName = "dev.json";

        ClassLoader classLoader = ValidateSqlGeneratorPrecision.class.getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource(resourceName)).getFile());

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return Arrays.asList(objectMapper.readValue(file, DbInput[].class));
        } catch (IOException e) {
            System.err.println("Error loading test data: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Test input data structure.
     */
    static class DbInput {
        @JsonProperty("db_id")
        private String dbId;

        @JsonProperty("question")
        private String question;

        @JsonProperty("evidence")
        private String evidence;

        @JsonProperty("SQL")
        private String sql;

        @JsonProperty("difficulty")
        private String difficulty;

        // Constructors, getters, and setters
        public DbInput() {
            // Empty constructor required for ObjectMapper
        }

        public DbInput(String dbId, String question, String evidence, String sql, String difficulty) {
            this.dbId = dbId;
            this.question = question;
            this.evidence = evidence;
            this.sql = sql;
            this.difficulty = difficulty;
        }

        public String getDbId() {
            return dbId;
        }

        public void setDbId(String dbId) {
            this.dbId = dbId;
        }

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public String getEvidence() {
            return evidence;
        }

        public void setEvidence(String evidence) {
            this.evidence = evidence;
        }

        public String getSql() {
            return sql;
        }

        public void setSql(String sql) {
            this.sql = sql;
        }

        public String getDifficulty() {
            return difficulty;
        }

        public void setDifficulty(String difficulty) {
            this.difficulty = difficulty;
        }

        @Override
        public String toString() {
            return "DbInput{" +
                "dbId='" + dbId + '\'' +
                ", question='" + question + '\'' +
                ", evidence='" + evidence + '\'' +
                ", sql='" + sql + '\'' +
                ", difficulty='" + difficulty + '\'' +
                '}';
        }
    }
}
