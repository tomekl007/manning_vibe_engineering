package manning.sqlgenerator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import manning.sqlgenerator.dto.SqlGeneratorQueryRequest;
import manning.sqlgenerator.dto.SqlGeneratorQueryResponse;
import manning.sqlgenerator.dto.Table;
import manning.sqlgenerator.service.SqlGeneratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SqlGeneratorController.class)
class SqlGeneratorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SqlGeneratorService sqlGeneratorService;

    @Autowired
    private ObjectMapper objectMapper;

    private SqlGeneratorQueryRequest validRequest;
    private SqlGeneratorQueryResponse successResponse;
    private SqlGeneratorQueryResponse errorResponse;

    @BeforeEach
    void setUp() {
        List<Table> tables = Arrays.asList(
                new Table("sales"),
                new Table("customers")
        );
        validRequest = new SqlGeneratorQueryRequest("Show me total sales by customer", tables);
        successResponse = SqlGeneratorQueryResponse.success("SELECT customer_id, SUM(sales_amount) as total_sales FROM sales GROUP BY customer_id");
        errorResponse = SqlGeneratorQueryResponse.error("Invalid request parameters");
    }

    @Test
    void testGenerateSql_Success() throws Exception {
        when(sqlGeneratorService.generateSql(any(SqlGeneratorQueryRequest.class))).thenReturn(successResponse);

        mockMvc.perform(post("/api/sql-generator/generate-sql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.sql").value(successResponse.getSql()))
                .andExpect(jsonPath("$.message").value("SQL generated successfully"));
    }

    @Test
    void testGenerateSql_InvalidRequest() throws Exception {
        SqlGeneratorQueryRequest invalidRequest = new SqlGeneratorQueryRequest();
        invalidRequest.setUserPrompt(""); // Empty text should fail validation

        mockMvc.perform(post("/api/sql-generator/generate-sql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGenerateSql_ServiceError() throws Exception {
        when(sqlGeneratorService.generateSql(any(SqlGeneratorQueryRequest.class))).thenReturn(errorResponse);

        mockMvc.perform(post("/api/sql-generator/generate-sql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid request parameters"));
    }

    @Test
    void testHealth() throws Exception {
        mockMvc.perform(get("/api/sql-generator/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("SQL Generator Service is running"));
    }

    @Test
    void testInfo() throws Exception {
        mockMvc.perform(get("/api/sql-generator/info"))
                .andExpect(status().isOk())
                .andExpect(content().string("SQL Generator Service v1.0.0 - Provides SQL generation functionality"));
    }
}
