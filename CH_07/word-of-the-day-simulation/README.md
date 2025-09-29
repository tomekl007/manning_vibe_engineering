# Gatling Performance Testing

This project contains Gatling performance tests for a words service API. The tests simulate load on the words service endpoints to measure performance under various conditions.

## Prerequisites

- Java 8 or higher
- Maven 3.6 or higher
- The words service should be running on `http://localhost:8080`

## Test Scenarios

The performance test includes two main scenarios:

1. **Word of the Day** - Tests the `/word-of-the-day` endpoint
   - Load: 1 user per second for 1 minute
   - Validates HTTP 200 response

2. **Word Exists Validation** - Tests the `/word-exists` endpoint
   - Load: 20 users per second for 1 minute
   - Uses random words from `words.csv` test data
   - Validates HTTP 200 response

## Running the Tests

### 1. Start the Target Service

Make sure the words service is running on `http://localhost:8080` before running the performance tests.

### 2. Run Gatling Tests

Navigate to the scala_project directory and run the Gatling tests:

```bash
cd scala_project
mvn gatling:test
```

### 3. Alternative: Run Specific Simulation

You can also run a specific simulation class:

```bash
mvn gatling:test -Dgatling.simulationClass=manning.performance.simulation.WordsSimulation
```

## Test Configuration

The test configuration is defined in `WordsSimulation.scala`:

- **Base URL**: `http://localhost:8080/words`
- **Accept Header**: `application/json`
- **Test Duration**: 1 minute per scenario
- **Load Pattern**: Constant users per second

## Test Data

The word validation scenario uses test data from `src/test/resources/words.csv`, which contains 100+ sample words for testing the word existence validation endpoint.

## Results

After running the tests, Gatling will generate:

1. **Console Output**: Real-time test execution statistics
2. **HTML Reports**: Detailed performance reports in `target/gatling-results/`
3. **Performance Metrics**: Response times, throughput, error rates, etc.

## Customizing Tests

To modify the test scenarios:

1. Edit `src/test/scala/manning/performance/simulation/WordsSimulation.scala`
2. Adjust load patterns, durations, or add new scenarios
3. Update test data in `src/test/resources/words.csv` if needed

## Maven Configuration

The project uses the Gatling Maven plugin with the following configuration:

- **Gatling Version**: 3.3.0
- **Plugin Version**: 3.1.0
- **Default Simulation**: `WordsSimulation`

## Troubleshooting

- **Connection Refused**: Ensure the target service is running on `http://localhost:8080`
- **Test Failures**: Check that the service endpoints are responding correctly
- **Memory Issues**: Adjust JVM heap size if needed: `export MAVEN_OPTS="-Xmx2g"`

### Corrupted JAR Files

If you encounter errors like `java.util.zip.ZipException: Invalid CEN header`, this indicates corrupted JAR files in your Maven local repository. Fix this by:

```bash
# Remove the corrupted dependency
rm -rf ~/.m2/repository/org/scalactic/scalactic_2.12/3.2.6/

# Clean and re-download dependencies
mvn clean
mvn gatling:test
```

If the issue persists, clean the entire Maven cache:

```bash
# Remove entire Maven local repository
rm -rf ~/.m2/repository/

# Clean and compile
mvn clean compile
mvn gatling:test
```

## Dependencies

The project includes the following key dependencies:

- `io.gatling:gatling-test-framework:3.3.0`
- `io.gatling.highcharts:gatling-charts-highcharts:3.3.0`
- `org.scala-lang:scala-library:2.12.10`
