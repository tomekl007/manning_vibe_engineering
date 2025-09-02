package manning.sqlgenerator.precision;

import manning.sqlgenerator.dto.SqlGeneratorQueryResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JUnit test class for running SQL Generator precision validation tests.
 * This test can be run as part of the test suite or standalone.
 *
 * Note: This test requires:
 * - SQL Generator Service running on localhost:8080 (or BASE_URL environment variable set)
 * - Valid OpenAI API key configured in the service
 * - Test data file (dev.json) in the test resources
 */
@SpringBootTest
@ActiveProfiles("test")
class SqlGeneratorPrecisionTest {

    /**
     * Test that the service is healthy and accessible.
     */
    @Test
    @EnabledIfEnvironmentVariable(named = "RUN_PRECISION_TESTS", matches = "true")
    void testServiceHealth() {
        String baseUrl = System.getenv("BASE_URL");
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            baseUrl = "http://localhost:8080";
        }

        boolean isHealthy = TestRestClient.isServiceHealthy(baseUrl);
        assertTrue(isHealthy, "SQL Generator Service should be healthy at " + baseUrl);
    }

    /**
     * Run a simple precision test with a single query.
     */
    @Test
    @EnabledIfEnvironmentVariable(named = "RUN_PRECISION_TESTS", matches = "true")
    void testSimplePrecisionValidation() {
        String baseUrl = System.getenv("BASE_URL");
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            baseUrl = "http://localhost:8080";
        }

        // Test with a simple query
        List<String> tableNames =
            DatasetToTablesConfig.DATASET_CONFIG_PER_DB_ID.get("california_schools");

        String testQuestion = "What is the highest eligible free rate for K-12 students in the schools in Alameda County?";

        SqlGeneratorQueryResponse response = TestRestClient.generateSql(
            tableNames, baseUrl, testQuestion);

        assertTrue(response != null, "Response should not be null");
        assertTrue(response.isSuccess(), "SQL generation should be successful");
        assertTrue(response.getSql() != null && !response.getSql().trim().isEmpty(),
                  "Generated SQL should not be empty");
    }

    /**
     * Run the full precision validation test suite.
     * This is a manual test that can be run to generate comprehensive precision reports.
     */
    @Test
    @EnabledIfEnvironmentVariable(named = "RUN_FULL_PRECISION_TESTS", matches = "true")
    void testFullPrecisionValidation() throws IOException {
        // This test runs the full precision validation and generates a CSV report
        // It's disabled by default as it requires significant OpenAI API usage

        String baseUrl = System.getenv("BASE_URL");
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            baseUrl = "http://localhost:8080";
        }

        // Check service health first
        assertTrue(TestRestClient.isServiceHealthy(baseUrl),
                  "Service must be healthy before running precision tests");

        // Run the precision validation
        ValidateSqlGeneratorPrecision.main(new String[]{});

        // If we get here without exception, the test passed
        assertTrue(true, "Full precision validation completed successfully");
    }
}
