# SQL Generator Service Precision Tests

This directory contains precision validation tests of the sql-generator-service REST API.

## Overview

The precision tests validate the quality of SQL generation by comparing generated SQL queries against expected results using similarity metrics:

- **Levenshtein Similarity**: Measures character-level similarity between queries
- **Overlap Similarity**: Measures word-level overlap between queries

## Test Structure

```
src/test/java/com/manning/sqlgenerator/integration/precision/
├── ValidateSqlGeneratorPrecision.java    # Main precision validation runner
├── SqlGeneratorPrecisionTest.java        # JUnit test wrapper
├── TestRestClient.java                   # REST API client for testing
├── DatasetConfigUtil.java                # Dataset configuration utilities
├── LevenshteinSimilarityCalculator.java  # Levenshtein distance calculator
├── OverlapSimilarityCalculator.java      # Word overlap calculator
└── QueryNormalizer.java                  # SQL query normalizer
```

## Prerequisites

1. **SQL Generator Service Running**: The service must be accessible (default: `http://localhost:8080`)
2. **OpenAI API Key**: Valid API key configured in the service
3. **Test Data**: `dev.json` file in test resources with sample queries

## Running the Tests

### Option 1: JUnit Tests (Recommended for CI/CD)

```bash
# Run basic precision tests
RUN_PRECISION_TESTS=true mvn test -Dtest=SqlGeneratorPrecisionTest

# Run full precision validation (generates CSV report)
RUN_FULL_PRECISION_TESTS=true mvn test -Dtest=SqlGeneratorPrecisionTest#testFullPrecisionValidation
```

### Option 2: Standalone Java Application

```bash
# Run the main precision validation
cd src/test/java
java -cp ".:../../../target/classes:../../../target/test-classes" \
     com.manning.sqlgenerator.integration.precision.ValidateSqlGeneratorPrecision
```

### Option 3: Custom Base URL

```bash
# Set custom service URL
export BASE_URL="http://your-service-host:8080"
RUN_PRECISION_TESTS=true mvn test -Dtest=SqlGeneratorPrecisionTest
```

## Test Data Format

The `dev.json` file contains test cases in this format:

```json
[
    {
        "db_id": "california_schools",
        "question": "What is the highest eligible free rate for K-12 students?",
        "evidence": "Eligible free rate = FRPM Count / Enrollment",
        "SQL": "SELECT FRPM_Count / Enrollment FROM frpm ORDER BY (FRPM_Count / Enrollment) DESC LIMIT 1",
        "difficulty": "simple"
    }
]
```

## Output

The precision tests generate a CSV report with columns:

- `db_id`: Database identifier
- `difficulty`: Query difficulty level
- `question`: User's question
- `expected`: Expected SQL query
- `actual`: Generated SQL query
- `levenshtein_similarity`: Character-level similarity score (0-1)
- `overlap_similarity`: Word-level similarity score (0-1)

## Configuration

### Environment Variables

- `BASE_URL`: Service base URL (default: `http://localhost:8080`)
- `RUN_PRECISION_TESTS`: Enable basic precision tests
- `RUN_FULL_PRECISION_TESTS`: Enable full precision validation

### Dataset Configuration

The `DatasetConfigUtil` class defines available datasets for each test case:

```java
DATASET_CONFIG_PER_DB_ID.put("california_schools", Arrays.asList(
    new DatasetConfig("frpm"),
    new DatasetConfig("satscores"),
    new DatasetConfig("schools")
));
```

## Similarity Metrics

### Levenshtein Similarity
- **Range**: 0.0 to 1.0 (1.0 = identical)
- **Calculation**: `1 - (edit_distance / max_length)`
- **Use Case**: Character-level accuracy assessment

### Overlap Similarity
- **Range**: 0.0 to 1.0 (1.0 = identical)
- **Calculation**: `intersection_size / union_size`
- **Use Case**: Word-level semantic similarity

## Query Normalization

Before similarity calculation, queries are normalized:

1. Remove catalog prefixes
2. Standardize whitespace
3. Normalize operators
4. Standardize quotes

## Integration with CI/CD

For continuous integration, use environment variables to control test execution:

```yaml
# GitHub Actions example
- name: Run Precision Tests
  env:
    RUN_PRECISION_TESTS: true
    BASE_URL: ${{ secrets.SERVICE_URL }}
  run: mvn test -Dtest=SqlGeneratorPrecisionTest
```

## Troubleshooting

### Common Issues

1. **Service Not Accessible**: Check if service is running and BASE_URL is correct
2. **OpenAI API Errors**: Verify API key and quota
3. **Test Data Missing**: Ensure `dev.json` is in test resources
4. **Memory Issues**: Increase JVM heap size for large test datasets

### Debug Mode

Enable debug logging in `application-test.yml`:

```yaml
logging:
  level:
    com.manning.sqlgenerator: DEBUG
```

## Performance Considerations

- **API Rate Limits**: Respect OpenAI API rate limits
- **Batch Processing**: Consider processing test cases in batches
- **Caching**: Cache generated SQL for repeated comparisons
- **Parallel Execution**: Run multiple test cases concurrently (with rate limiting)

## Extending the Tests

### Adding New Test Cases

1. Add entries to `dev.json`
2. Update `DatasetConfigUtil` with new dataset configurations
3. Run tests to validate new cases

### Custom Similarity Metrics

1. Implement new calculator class
2. Add metric to CSV output
3. Update `ValidateSqlGeneratorPrecision.showResultAndSave()`

### New Dataset Types

1. Extend `DatasetConfig` class
2. Update conversion logic in `TestRestClient`
3. Modify normalization in `QueryNormalizer`
