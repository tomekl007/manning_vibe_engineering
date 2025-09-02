package manning.sqlgenerator.precision;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Map;

/**
 * Test class for SqlFileParser utility.
 */
public class SqlFileParserTest {

    @Test
    public void testParseCaliforniaSchoolsSql() throws IOException {
        Map<String, String> tableDefinitions = SqlFileParser.parseSqlFile("california_schools.sql");
        
        assertNotNull(tableDefinitions);
        assertEquals(3, tableDefinitions.size());
        
        // Check that all expected tables are present
        assertTrue(tableDefinitions.containsKey("frpm"));
        assertTrue(tableDefinitions.containsKey("satscores"));
        assertTrue(tableDefinitions.containsKey("schools"));
        
        // Check that CREATE TABLE statements are captured
        String frpmTable = tableDefinitions.get("frpm");
        assertNotNull(frpmTable);
        assertTrue(frpmTable.startsWith("CREATE TABLE"));
        assertTrue(frpmTable.contains("CDSCode"));
        assertTrue(frpmTable.contains("VARCHAR"));
    }

    @Test
    public void testParseDebitCardSpecializingSql() throws IOException {
        Map<String, String> tableDefinitions = SqlFileParser.parseSqlFile("debit_card_specializing.sql");
        
        assertNotNull(tableDefinitions);
        assertEquals(5, tableDefinitions.size());
        
        // Check that all expected tables are present
        assertTrue(tableDefinitions.containsKey("customers"));
        assertTrue(tableDefinitions.containsKey("gasstations"));
        assertTrue(tableDefinitions.containsKey("products"));
        assertTrue(tableDefinitions.containsKey("yearmonth"));
        assertTrue(tableDefinitions.containsKey("transactions_1k"));
        
        // Check that CREATE TABLE statements are captured
        String customersTable = tableDefinitions.get("customers");
        assertNotNull(customersTable);
        assertTrue(customersTable.startsWith("CREATE TABLE"));
        assertTrue(customersTable.contains("CustomerID"));
    }

    @Test
    public void testParseThrombosisPredictionSql() throws IOException {
        Map<String, String> tableDefinitions = SqlFileParser.parseSqlFile("thrombosis_prediction.sql");
        
        assertNotNull(tableDefinitions);
        assertEquals(5, tableDefinitions.size());
        
        // Check that all expected tables are present
        assertTrue(tableDefinitions.containsKey("Examination"));
        assertTrue(tableDefinitions.containsKey("Patient"));
        assertTrue(tableDefinitions.containsKey("Laboratory"));
        assertTrue(tableDefinitions.containsKey("directors"));
        assertTrue(tableDefinitions.containsKey("movies"));
        
        // Check that CREATE TABLE statements are captured
        String examinationTable = tableDefinitions.get("Examination");
        assertNotNull(examinationTable);
        assertTrue(examinationTable.startsWith("CREATE TABLE"));
        assertTrue(examinationTable.contains("ID"));
    }
}
