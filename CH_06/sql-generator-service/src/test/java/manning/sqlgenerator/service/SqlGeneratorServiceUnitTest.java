package manning.sqlgenerator.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import manning.sqlgenerator.dto.SqlGeneratorQueryRequest;
import manning.sqlgenerator.dto.SqlGeneratorQueryResponse;
import manning.sqlgenerator.dto.Table;
import manning.sqlgenerator.service.llm.LanguageModelClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SqlGeneratorServiceUnitTest {

  private SqlGeneratorServiceImpl sqlGeneratorService;
  private SqlGeneratorQueryRequest validRequest;
  private List<Table> tables;

  @BeforeEach
  void setUp() {
    sqlGeneratorService = new SqlGeneratorServiceImpl(Collections.emptyList(), "openai");
    tables =
        Arrays.asList(
            new Table("sales", "CREATE TABLE sales"), new Table("customers", "CREATE TABLE customers"));
    validRequest = new SqlGeneratorQueryRequest("Show me total sales by customer", tables);
  }

  @Test
  void testBuildPrompt() {
    String prompt = sqlGeneratorService.buildPrompt(validRequest);

    assertNotNull(prompt);
    assertTrue(prompt.contains("Show me total sales by customer"));
    assertTrue(prompt.contains("sales"));
    assertTrue(prompt.contains("customers"));
    assertTrue(prompt.contains("You are a SQL expert"));
  }

  @Test
  void testCleanSqlResponse_WithMarkdown() {
    String response = "```sql\nSELECT * FROM table\n```";
    String cleaned = sqlGeneratorService.cleanSqlResponse(response);

    assertEquals("SELECT * FROM table", cleaned);
  }

  @Test
  void testCleanSqlResponse_WithoutMarkdown() {
    String response = "SELECT * FROM table";
    String cleaned = sqlGeneratorService.cleanSqlResponse(response);

    assertEquals("SELECT * FROM table", cleaned);
  }

  @Test
  void testGenerateSql_WithUnsupportedProvider() {
    SqlGeneratorServiceImpl service = new SqlGeneratorServiceImpl(Collections.emptyList(), "unknown");

    SqlGeneratorQueryResponse response = service.generateSql(validRequest);

    assertFalse(response.isSuccess());
    assertTrue(response.getMessage().contains("unknown"));
  }

  @Test
  void testGenerateSql_WithWorkingProvider() {
    LanguageModelClient stubClient = new StubClient("gemini", true, "```sql\nSELECT 1\n```");
    SqlGeneratorServiceImpl service =
        new SqlGeneratorServiceImpl(List.of(stubClient), "gemini");

    SqlGeneratorQueryResponse response = service.generateSql(validRequest);

    assertTrue(response.isSuccess());
    assertEquals("SELECT 1", response.getSql());
  }

  private static class StubClient implements LanguageModelClient {

    private final String name;
    private final boolean enabled;
    private final String response;

    StubClient(String name, boolean enabled, String response) {
      this.name = name;
      this.enabled = enabled;
      this.response = response;
    }

    @Override
    public String getProviderName() {
      return name;
    }

    @Override
    public boolean isEnabled() {
      return enabled;
    }

    @Override
    public String generateText(String prompt) {
      return response;
    }
  }
}
