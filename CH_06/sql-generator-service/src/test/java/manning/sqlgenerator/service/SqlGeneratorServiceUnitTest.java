package manning.sqlgenerator.service;

import manning.sqlgenerator.dto.SqlGeneratorQueryRequest;
import manning.sqlgenerator.dto.SqlGeneratorQueryResponse;
import manning.sqlgenerator.dto.Table;
import manning.sqlgenerator.service.impl.SqlGeneratorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
                new Table("sales"),
                new Table("customers")
        );
        validRequest = new SqlGeneratorQueryRequest("Show me total sales by customer", tables);
    }

    @Test
    void testBuildPrompt() {
        // Use reflection to access private method
        String prompt = (String) ReflectionTestUtils.invokeMethod(
                sqlGeneratorService, "buildPrompt", validRequest);

        assertNotNull(prompt);
        assertTrue(prompt.contains("Show me total sales by customer"));
        assertTrue(prompt.contains("sales"));
        assertTrue(prompt.contains("customers"));
        assertTrue(prompt.contains("You are a SQL expert"));
    }

    @Test
    void testCleanSqlResponse_WithMarkdown() {
        String response = "```sql\nSELECT * FROM table\n```";
        String cleaned = (String) ReflectionTestUtils.invokeMethod(
                sqlGeneratorService, "cleanSqlResponse", response);

        assertEquals("SELECT * FROM table", cleaned);
    }

    @Test
    void testCleanSqlResponse_WithoutMarkdown() {
        String response = "SELECT * FROM table";
        String cleaned = (String) ReflectionTestUtils.invokeMethod(
                sqlGeneratorService, "cleanSqlResponse", response);

        assertEquals("SELECT * FROM table", cleaned);
    }

    @Test
    void testCleanSqlResponse_MultipleLines() {
        String response = "Here is your SQL:\nSELECT * FROM table\nHope this helps!";
        String cleaned = (String) ReflectionTestUtils.invokeMethod(
                sqlGeneratorService, "cleanSqlResponse", response);

        assertEquals("SELECT * FROM table", cleaned);
    }

    @Test
    void testCleanSqlResponse_Null() {
        String cleaned = (String) ReflectionTestUtils.invokeMethod(
                sqlGeneratorService, "cleanSqlResponse", (String) null);

        assertNull(cleaned);
    }

    @Test
    void testCleanSqlResponse_Empty() {
        String cleaned = (String) ReflectionTestUtils.invokeMethod(
                sqlGeneratorService, "cleanSqlResponse", "");

        assertEquals("", cleaned);
    }
}
