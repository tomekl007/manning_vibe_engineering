package manning.sqlgenerator.precision;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import manning.sqlgenerator.precision.ValidateSqlGeneratorPrecision.DbInput;

/**
 * Loads prompt examples from a frozen pool ({@code examples.json}), kept separate from the
 * evaluation set in {@code dev.json}.
 *
 * <p>Callers must pass the SQL of the query currently under evaluation so it can be excluded
 * from the returned examples (defense in depth if the pools ever overlap).
 */
public final class DevJsonQueryExtractor {

  private static final String EXAMPLES_RESOURCE = "examples.json";

  private static List<DbInput> exampleEntries;

  private DevJsonQueryExtractor() {}

  /**
   * Loads the frozen example pool from classpath resources. Safe to call multiple times; the pool
   * is loaded once and reused.
   */
  public static synchronized void loadFrozenExamplePool() throws IOException {
    if (exampleEntries != null) {
      return;
    }
    ClassLoader classLoader = DevJsonQueryExtractor.class.getClassLoader();
    File file =
        new File(Objects.requireNonNull(classLoader.getResource(EXAMPLES_RESOURCE)).getFile());
    ObjectMapper objectMapper = new ObjectMapper();
    exampleEntries = Arrays.asList(objectMapper.readValue(file, DbInput[].class));
  }

  /**
   * Visible for tests: replaces the frozen pool with the given entries.
   */
  static synchronized void setExampleEntriesForTests(List<DbInput> entries) {
    exampleEntries = entries;
  }

  static synchronized void resetForTests() {
    exampleEntries = null;
  }

  /**
   * Returns up to {@code n} example SQL queries for {@code dbId} from the frozen pool, excluding
   * any entry whose SQL equals {@code sqlToExclude} (the query currently under evaluation).
   *
   * @param dbId database identifier
   * @param n maximum number of examples to return
   * @param sqlToExclude gold SQL of the current evaluation item; may be null
   * @return example SQL queries (order preserved from the frozen pool)
   */
  public static List<String> getExampleQueries(String dbId, int n, String sqlToExclude) {
    if (exampleEntries == null || exampleEntries.isEmpty() || n <= 0) {
      return new ArrayList<>();
    }

    String normalizedExclude = normalizeSql(sqlToExclude);

    return exampleEntries.stream()
        .filter(entry -> dbId.equals(entry.getDbId()))
        .map(DbInput::getSql)
        .filter(sql -> sql != null && !sql.trim().isEmpty())
        .filter(sql -> normalizedExclude == null || !normalizedExclude.equals(normalizeSql(sql)))
        .limit(n)
        .collect(Collectors.toList());
  }

  /**
   * Returns up to {@link ValidateSqlGeneratorPrecision#NR_OF_LAST_QUERIES_INCLUDED} example SQL
   * queries for {@code dbId}, excluding {@code sqlToExclude}.
   */
  public static List<String> getExampleQueries(String dbId, String sqlToExclude) {
    return getExampleQueries(
        dbId, ValidateSqlGeneratorPrecision.NR_OF_LAST_QUERIES_INCLUDED, sqlToExclude);
  }

  /**
   * Keys used to detect overlap between the frozen example pool and the evaluation set.
   */
  public static List<ExampleKey> exampleKeys() {
    if (exampleEntries == null) {
      return List.of();
    }
    return exampleEntries.stream()
        .map(e -> new ExampleKey(e.getDbId(), e.getQuestion(), normalizeSql(e.getSql())))
        .collect(Collectors.toList());
  }

  private static String normalizeSql(String sql) {
    if (sql == null) {
      return null;
    }
    return sql.trim().replaceAll("\\s+", " ");
  }

  /** Identity of an example used to keep eval and example pools disjoint. */
  public record ExampleKey(String dbId, String question, String normalizedSql) {}
}
