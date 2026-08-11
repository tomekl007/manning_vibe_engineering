package manning.sqlgenerator.precision;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import manning.sqlgenerator.precision.ValidateSqlGeneratorPrecision.DbInput;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DevJsonQueryExtractorTest {

  @BeforeEach
  void setUp() {
    DevJsonQueryExtractor.setExampleEntriesForTests(
        List.of(
            new DbInput("california_schools", "Q1", "", "SELECT 1", "simple"),
            new DbInput("california_schools", "Q2", "", "SELECT 2", "simple"),
            new DbInput("california_schools", "Q3", "", "SELECT 3", "simple"),
            new DbInput("debit_card_specializing", "Q4", "", "SELECT 4", "simple")));
  }

  @AfterEach
  void tearDown() {
    DevJsonQueryExtractor.resetForTests();
  }

  @Test
  void returnsExamplesFromFrozenPoolOnlyForRequestedDb() {
    List<String> examples =
        DevJsonQueryExtractor.getExampleQueries("california_schools", 2, "SELECT 99");

    assertEquals(List.of("SELECT 1", "SELECT 2"), examples);
  }

  @Test
  void excludesCurrentEvaluationSqlFromExamples() {
    List<String> examples =
        DevJsonQueryExtractor.getExampleQueries("california_schools", 2, "SELECT 1");

    assertEquals(List.of("SELECT 2", "SELECT 3"), examples);
    assertFalse(examples.contains("SELECT 1"));
  }

  @Test
  void excludesCurrentEvaluationSqlIgnoringWhitespace() {
    List<String> examples =
        DevJsonQueryExtractor.getExampleQueries("california_schools", 5, "  SELECT   2  ");

    assertTrue(examples.contains("SELECT 1"));
    assertFalse(examples.contains("SELECT 2"));
  }

  @Test
  void loadsClasspathExamplesJson() throws Exception {
    DevJsonQueryExtractor.resetForTests();
    DevJsonQueryExtractor.loadFrozenExamplePool();

    List<String> examples =
        DevJsonQueryExtractor.getExampleQueries("california_schools", 20, "not-a-real-sql");

    assertEquals(20, examples.size());
  }
}
