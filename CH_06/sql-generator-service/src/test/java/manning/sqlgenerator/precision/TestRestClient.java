package manning.sqlgenerator.precision;

import com.fasterxml.jackson.databind.ObjectMapper;

import manning.sqlgenerator.dto.SqlGeneratorQueryRequest;
import manning.sqlgenerator.dto.SqlGeneratorQueryResponse;
import manning.sqlgenerator.dto.Table;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST client utility for testing the sql-generator-service.
 * Replaces the original gRPC client for precision testing.
 */
public class TestRestClient {

    private static final RestTemplate restTemplate = new RestTemplate();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Converts table names to Table DTOs for the REST API.
     */
    private static List<Table> convertTableNamesToTables(List<String> tableNames) {
        return tableNames.stream()
            .map(Table::new)
            .collect(Collectors.toList());
    }

    /**
     * Calls the sql-generator-service to generate SQL using REST API.
     *
     * @param tableNames List of table names
     * @param baseUrl Base URL of the sql-generator-service
     * @param userPrompt The user's question/prompt
     * @return The generated SQL response
     */
    public static SqlGeneratorQueryResponse generateSql(
            List<String> tableNames,
            String baseUrl,
            String userPrompt) {

        try {
            // Convert table names to Table DTOs
            List<Table> tables = convertTableNamesToTables(tableNames);

            // Create the request
            SqlGeneratorQueryRequest request = new SqlGeneratorQueryRequest(userPrompt, tables);

            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create HTTP entity
            HttpEntity<SqlGeneratorQueryRequest> entity = new HttpEntity<>(request, headers);

            // Make the REST call
            String url = baseUrl + "/api/sql-generator/generate-sql";
            ResponseEntity<SqlGeneratorQueryResponse> response = restTemplate.postForEntity(
                url, entity, SqlGeneratorQueryResponse.class);

            System.out.println("REST API request: " + request);
            System.out.println("REST API response: " + response.getBody());

            return response.getBody();

        } catch (Exception ex) {
            System.out.println("Error calling REST API: " + ex.getMessage());
            ex.printStackTrace();

            // Return error response
            return SqlGeneratorQueryResponse.error("Failed to call REST API: " + ex.getMessage());
        }
    }

    /**
     * Health check for the sql-generator-service.
     */
    public static boolean isServiceHealthy(String baseUrl) {
        try {
            String url = baseUrl + "/api/sql-generator/health";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception ex) {
            System.out.println("Health check failed: " + ex.getMessage());
            return false;
        }
    }
}
