package manning.sqlgenerator.controller;

import manning.sqlgenerator.dto.SqlGeneratorQueryRequest;
import manning.sqlgenerator.dto.SqlGeneratorQueryResponse;
import manning.sqlgenerator.service.SqlGeneratorService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sql-generator")
@CrossOrigin(origins = "*")
public class SqlGeneratorController {

    private static final Logger logger = LoggerFactory.getLogger(SqlGeneratorController.class);

    private final SqlGeneratorService sqlGeneratorService;

    @Autowired
    public SqlGeneratorController(SqlGeneratorService sqlGeneratorService) {
        this.sqlGeneratorService = sqlGeneratorService;
    }

    @PostMapping("/generate-sql")
    public ResponseEntity<SqlGeneratorQueryResponse> generateSql(
            @Valid @RequestBody SqlGeneratorQueryRequest request) {
        logger.info("Received SQL generation request");

        try {
            SqlGeneratorQueryResponse response = sqlGeneratorService.generateSql(request);

            if (response.isSuccess()) {
                logger.info("Successfully processed request");
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Request failed - {}", response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            logger.error("Unexpected error processing request", e);
            SqlGeneratorQueryResponse errorResponse = SqlGeneratorQueryResponse.error("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("SQL Generator Service is running");
    }

    @GetMapping("/info")
    public ResponseEntity<String> getServiceInfo() {
        return ResponseEntity.ok("SQL Generator Service v1.0.0 - Provides SQL generation functionality");
    }
}
