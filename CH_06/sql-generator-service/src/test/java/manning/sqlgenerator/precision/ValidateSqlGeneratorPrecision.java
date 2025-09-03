package manning.sqlgenerator.precision;

import static manning.sqlgenerator.precision.DatasetToTablesConfig.getTableDefinitions;
import static manning.sqlgenerator.precision.TestRestClient.generateSqlWithTables;
import static manning.sqlgenerator.precision.TestRestClient.isServiceHealthy;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import manning.sqlgenerator.dto.SqlGeneratorQueryResponse;
import manning.sqlgenerator.dto.Table;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.ArrayList;

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
 * Includes complete table structure information from SQL files.
 */
public class ValidateSqlGeneratorPrecision {

    private static final String DEFAULT_BASE_URL = "http://localhost:8080";
    public static final int NR_OF_LAST_QUERIES_INCLUDED = 2;
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

        // Initialize the query extractor with the loaded data
        DevJsonQueryExtractor.setDbInputs(dbInputs);

        // Filter inputs to only include those with available dataset configs
        List<DbInput> inputSubset = dbInputs.stream()
            .filter(extractDBs())
//            .limit(4)
            .collect(Collectors.toList());

        System.out.println("Will run the SQL generation for " + inputSubset.size() + " number of inputs.");

        // Create output file
        File tempDirectory = new File(System.getProperty("java.io.tmpdir"));
        Path filePath = Files.createTempFile(
            tempDirectory.toPath(),
            "sql_generator_precision_result_last_"+NR_OF_LAST_QUERIES_INCLUDED + "_queries",
            "_" + Instant.now().toEpochMilli() + ".csv"
        );

        System.out.println("The results will be saved to: " + filePath);

        // Create summary file path
        Path summaryFilePath = Files.createTempFile(
            tempDirectory.toPath(),
            "sql_generator_precision_summary_last_"+NR_OF_LAST_QUERIES_INCLUDED + "_queries",
            "_" + Instant.now().toEpochMilli() + ".csv"
        );
        System.out.println("The summary will be saved to: " + summaryFilePath);

        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath.toString()));
             CSVWriter summaryWriter = new CSVWriter(new FileWriter(summaryFilePath.toString()))) {
            // Write the detailed results header
            String[] header = {
                "db_id",
                "difficulty",
                "question",
                "expected",
                "actual",
                "overlap_similarity"
            };
            writer.writeNext(header);

            // Write the summary header
            String[] summaryHeader = {
                "db_id",
                "difficulty",
                "p50_overlap_similarity",
                "p90_overlap_similarity",
                "p99_overlap_similarity"
            };
            summaryWriter.writeNext(summaryHeader);

            // Process each input and collect results for summary
            List<SqlGenerationResult> results = new ArrayList<>();
            String finalBaseUrl = baseUrl;
            inputSubset.stream()
                .map(input -> processDbInput(input, finalBaseUrl))
                .forEach(result -> {
                    showResultAndSave(result, writer);
                    results.add(result);
                });

            // Generate and save summary
            generateAndSaveSummary(results, summaryWriter);
        }

        System.out.println("Precision validation completed. Results saved to: " + filePath);
        System.out.println("Summary saved to: " + summaryFilePath);
    }

    /**
     * Filters database inputs to only include those with available dataset configurations.
     */
    private static Predicate<DbInput> extractDBs() {
        return input -> getTableDefinitions(input.getDbId()) != null;
    }

    /**
     * Processes a single database input and generates SQL.
     */
    private static SqlGenerationResult processDbInput(DbInput input, String baseUrl) {
        System.out.println("Processing: " + input.getDbId() + " - " + input.getQuestion());

        // Get the complete table definitions for this database
        List<Table> tables = getTableDefinitions(input.getDbId());
        if (tables == null) {
            System.out.println("No dataset configuration found for: " + input.getDbId());
            return new SqlGenerationResult(null, input);
        }

        System.out.println("Using complete table definitions with CREATE TABLE SQL for: " + input.getDbId());

        // Get the last N queries for the database from dev.json
        List<String> lastQueries = DevJsonQueryExtractor.getLastNQueries(input.getDbId());
        if (!lastQueries.isEmpty()) {
            System.out.println("Including " + lastQueries + " recent queries for context from dev.json");
        }

        // Call the REST API to generate SQL with enhanced table information and query history
        SqlGeneratorQueryResponse response = generateSqlWithTables(tables, baseUrl, input.getQuestion(), lastQueries);

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
        String normalized = QueryNormalizer.normalize(result.getActualSql());

        // Calculate similarity score
        double overlapSimilarity = OverlapSimilarityCalculator.calculateQuerySimilarity(
            result.getDbInput().getSql(), normalized);

        // Write to CSV
        String[] dataRow = {
            result.getDbInput().getDbId(),
            result.getDbInput().getDifficulty(),
            result.getDbInput().getQuestion(),
            result.getDbInput().getSql(),
            normalized,
            String.valueOf(overlapSimilarity)
        };

        writer.writeNext(dataRow);

        // Print similarity score
        System.out.println("Overlap similarity: " + overlapSimilarity);
        System.out.println("---");
    }

    /**
     * Generates and saves a summary CSV file grouped by db_id and difficulty.
     */
    private static void generateAndSaveSummary(List<SqlGenerationResult> results, CSVWriter summaryWriter) {
        if (results.isEmpty()) {
            System.out.println("No results to generate summary.");
            return;
        }

        try {
            // Group results by db_id and difficulty
            Map<String, Map<String, List<SqlGenerationResult>>> groupedResults = results.stream()
                .collect(Collectors.groupingBy(
                    result -> result.getDbInput().getDbId(),
                    Collectors.groupingBy(result -> result.getDbInput().getDifficulty())
                ));

            // Calculate and write summary for each group
            for (Map.Entry<String, Map<String, List<SqlGenerationResult>>> dbEntry : groupedResults.entrySet()) {
                String dbId = dbEntry.getKey();
                for (Map.Entry<String, List<SqlGenerationResult>> difficultyEntry : dbEntry.getValue().entrySet()) {
                    String difficulty = difficultyEntry.getKey();
                    List<SqlGenerationResult> difficultyResults = difficultyEntry.getValue();

                    if (difficultyResults.isEmpty()) {
                        continue;
                    }

                    // Calculate percentiles for overlap similarity for this group
                    List<Double> similarities = difficultyResults.stream()
                        .mapToDouble(result -> {
                            try {
                                return OverlapSimilarityCalculator.calculateQuerySimilarity(
                                    result.getDbInput().getSql(), 
                                    QueryNormalizer.normalize(result.getActualSql())
                                );
                            } catch (Exception e) {
                                return 0.0; // Return 0.0 if calculation fails
                            }
                        })
                        .boxed()
                        .sorted()
                        .collect(Collectors.toList());

                    if (!similarities.isEmpty()) {
                        double p50 = calculatePercentile(similarities, 50);
                        double p90 = calculatePercentile(similarities, 90);
                        double p99 = calculatePercentile(similarities, 99);

                        String[] summaryRow = {dbId, difficulty, String.valueOf(p50), String.valueOf(p90), String.valueOf(p99)};
                        summaryWriter.writeNext(summaryRow);
                        
                        System.out.println("Summary: " + dbId + " - " + difficulty + " | P50: " + p50 + " | P90: " + p90 + " | P99: " + p99);
                    }
                }
            }
            summaryWriter.flush(); // Ensure all data is written
        } catch (Exception e) {
            System.err.println("Error generating summary: " + e.getMessage());
            e.printStackTrace();
        }
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
     * Calculates the specified percentile from a sorted list of values.
     * 
     * @param sortedValues List of values sorted in ascending order
     * @param percentile Percentile to calculate (0-100)
     * @return The value at the specified percentile
     */
    private static double calculatePercentile(List<Double> sortedValues, int percentile) {
        if (sortedValues == null || sortedValues.isEmpty()) {
            return 0.0;
        }
        
        if (percentile <= 0) {
            return sortedValues.get(0);
        }
        
        if (percentile >= 100) {
            return sortedValues.get(sortedValues.size() - 1);
        }
        
        double index = (percentile / 100.0) * (sortedValues.size() - 1);
        int lowerIndex = (int) Math.floor(index);
        int upperIndex = (int) Math.ceil(index);
        
        if (lowerIndex == upperIndex) {
            return sortedValues.get(lowerIndex);
        }
        
        // Linear interpolation between the two nearest values
        double lowerValue = sortedValues.get(lowerIndex);
        double upperValue = sortedValues.get(upperIndex);
        double weight = index - lowerIndex;
        
        return lowerValue + weight * (upperValue - lowerValue);
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
    public static class DbInput {
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
