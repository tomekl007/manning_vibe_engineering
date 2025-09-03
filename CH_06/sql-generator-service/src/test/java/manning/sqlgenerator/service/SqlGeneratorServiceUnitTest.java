package manning.sqlgenerator.service;

import manning.sqlgenerator.dto.SqlGeneratorQueryRequest;
import manning.sqlgenerator.dto.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SqlGeneratorServiceUnitTest {

    @InjectMocks
    private SqlGeneratorServiceImpl sqlGeneratorService;

    private SqlGeneratorQueryRequest validRequest;
    private List<Table> tables;

    @BeforeEach
    void setUp() {
        tables = Arrays.asList(
                new Table("sales", "CREATE TABLE sales"),
                new Table("customers", "CREATE TABLE customers")
        );
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

}
