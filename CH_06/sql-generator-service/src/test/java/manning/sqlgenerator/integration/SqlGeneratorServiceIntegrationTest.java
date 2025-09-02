package manning.sqlgenerator.integration;

import manning.sqlgenerator.dto.SqlGeneratorQueryRequest;
import manning.sqlgenerator.dto.SqlGeneratorQueryResponse;
import manning.sqlgenerator.dto.Table;
import manning.sqlgenerator.service.SqlGeneratorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test for the SQL Generator service.
 * Tests the complete flow from service layer to response.
 * Note: This test requires a valid OpenAI API key to run successfully.
 */
@SpringBootTest
@ActiveProfiles("test")
class SqlGeneratorServiceIntegrationTest {

  @Autowired
  private SqlGeneratorService sqlGeneratorService;

  @Test
  void testGenerateSql_Integration() {
    // Create test data
    List<Table> tables = Arrays.asList(
        new Table("sales"),
        new Table("customers")
    );

    SqlGeneratorQueryRequest request = new SqlGeneratorQueryRequest(
        "Show me the top 5 customers by total sales",
        tables
    );

    // Execute the service
    SqlGeneratorQueryResponse response = sqlGeneratorService.generateSql(request);

    // Verify response structure
    assertNotNull(response, "Response should not be null");
    assertNotNull(response.getMessage(), "Message should not be null");

    assertTrue(response.isSuccess());
    assertNotNull(response.getSql(), "SQL should not be null when successful");
    assertFalse(response.getSql().trim().isEmpty(), "SQL should not be empty when successful");
    assertTrue(response.getSql().toUpperCase().contains("SELECT"),
        "Generated SQL should contain SELECT statement");

  }

  @Test
  void testGenerateSql_WithComplexRequest() {
    // Create a more complex test request
    List<Table> tables = Arrays.asList(
        new Table("user_behavior"),
        new Table("product_metrics")
    );

    SqlGeneratorQueryRequest request = new SqlGeneratorQueryRequest(
        "Find users who purchased products with high ratings and show their total spending",
        tables
    );

    // Execute the service
    SqlGeneratorQueryResponse response = sqlGeneratorService.generateSql(request);

    // Verify response structure
    assertNotNull(response, "Response should not be null");
    assertNotNull(response.getMessage(), "Message should not be null");

    assertNotNull(response.getSql(), "SQL should not be null when successful");
    assertFalse(response.getSql().trim().isEmpty(), "SQL should not be empty when successful");

    // The generated SQL should be complex enough for this request
    String sql = response.getSql().toUpperCase();
    assertTrue(sql.contains("SELECT") || sql.contains("WITH"),
        "Generated SQL should contain SELECT or WITH statement");

  }

  @Test
  void testGenerateSql_WithMinimalRequest() {
    // Test with minimal required fields
    List<Table> tables = Arrays.asList(
        new Table("simple_table")
    );

    SqlGeneratorQueryRequest request = new SqlGeneratorQueryRequest(
        "Count all records",
        tables
    );

    // Execute the service
    SqlGeneratorQueryResponse response = sqlGeneratorService.generateSql(request);

    // Verify response structure
    assertNotNull(response, "Response should not be null");
    assertNotNull(response.getMessage(), "Message should not be null");

    assertTrue(response.isSuccess());
    assertNotNull(response.getSql(), "SQL should not be null when successful");
    assertFalse(response.getSql().trim().isEmpty(), "SQL should not be empty when successful");

    // Should generate a simple COUNT query
    String sql = response.getSql().toUpperCase();
    assertTrue(sql.contains("COUNT") || sql.contains("SELECT"),
        "Generated SQL should contain COUNT or SELECT for counting records");

  }
}
