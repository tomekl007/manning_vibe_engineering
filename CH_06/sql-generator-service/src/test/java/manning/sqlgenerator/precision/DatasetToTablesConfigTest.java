package manning.sqlgenerator.precision;

import manning.sqlgenerator.dto.Table;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Test class for enhanced DatasetToTablesConfig.
 */
public class DatasetToTablesConfigTest {

    @Test
    public void testGetTableDefinitions() {
        // Test california_schools dataset
        List<Table> californiaTables = DatasetToTablesConfig.getTableDefinitions("california_schools");
        assertNotNull(californiaTables);
        assertEquals(3, californiaTables.size());
        
        // Check that tables have complete information
        Table frpmTable = californiaTables.stream()
            .filter(t -> "frpm".equals(t.getTableName()))
            .findFirst()
            .orElse(null);
        
        assertNotNull(frpmTable);
        assertNotNull(frpmTable.getCreateTableSql());
        assertTrue(frpmTable.getCreateTableSql().contains("CREATE TABLE"));
        assertTrue(frpmTable.getCreateTableSql().contains("CDSCode"));
        
        // Test debit_card_specializing dataset
        List<Table> debitCardTables = DatasetToTablesConfig.getTableDefinitions("debit_card_specializing");
        assertNotNull(debitCardTables);
        assertEquals(5, debitCardTables.size());
        
        // Test thrombosis_prediction dataset
        List<Table> thrombosisTables = DatasetToTablesConfig.getTableDefinitions("thrombosis_prediction");
        assertNotNull(thrombosisTables);
        assertEquals(5, thrombosisTables.size());
    }

    @Test
    public void testGetTableNames() {
        // Test california_schools dataset
        List<String> californiaTableNames = DatasetToTablesConfig.getTableNames("california_schools");
        assertNotNull(californiaTableNames);
        assertEquals(3, californiaTableNames.size());
        assertTrue(californiaTableNames.contains("frpm"));
        assertTrue(californiaTableNames.contains("satscores"));
        assertTrue(californiaTableNames.contains("schools"));
        
        // Test debit_card_specializing dataset
        List<String> debitCardTableNames = DatasetToTablesConfig.getTableNames("debit_card_specializing");
        assertNotNull(debitCardTableNames);
        assertEquals(5, debitCardTableNames.size());
        assertTrue(debitCardTableNames.contains("customers"));
        assertTrue(debitCardTableNames.contains("gasstations"));
        assertTrue(debitCardTableNames.contains("products"));
        assertTrue(debitCardTableNames.contains("yearmonth"));
        assertTrue(debitCardTableNames.contains("transactions_1k"));
    }

    @Test
    public void testTableStructure() {
        List<Table> tables = DatasetToTablesConfig.getTableDefinitions("california_schools");
        
        for (Table table : tables) {
            // Each table should have a name
            assertNotNull(table.getTableName());
            assertFalse(table.getTableName().trim().isEmpty());
            
            // Each table should have CREATE TABLE SQL
            assertNotNull(table.getCreateTableSql());
            assertTrue(table.getCreateTableSql().contains("CREATE TABLE"));
            assertTrue(table.getCreateTableSql().contains(table.getTableName()));
        }
    }

    @Test
    public void testNonExistentDataset() {
        assertNull(DatasetToTablesConfig.getTableDefinitions("non_existent_dataset"));
        assertNull(DatasetToTablesConfig.getTableNames("non_existent_dataset"));
    }
}
