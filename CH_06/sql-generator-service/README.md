# SQL Generator Service

A standalone Spring Boot service that provides SQL generation functionality for the `generateSql` feature.

## Configuration

The service uses a simplified configuration approach:

- **`application.yml`** - Main configuration for all environments
- **`application-test.yml`** - Test-specific configuration (uses random ports, debug logging)

### Environment Variables

Set these environment variables to configure the service:

```bash
export OPENAI_API_KEY='your-openai-api-key-here'
export OPENAI_MODEL='gpt-3.5-turbo'  # Optional, defaults to gpt-3.5-turbo
export OPENAI_TIMEOUT='60'            # Optional, defaults to 60 seconds
```

## API Endpoints

### Main Endpoint
- **POST** `/api/sql-generator/generate-sql`
  - Generates SQL based on user input and table information
  - Request body: `SqlGeneratorQueryRequest`
  - Response: `SqlGeneratorQueryResponse`

### Health and Info
- **GET** `/api/sql-generator/health` - Service health check
- **GET** `/api/sql-generator/info` - Service information
- **GET** `/actuator/health` - Spring Actuator health endpoint
- **GET** `/actuator/info` - Spring Actuator info endpoint
- **GET** `/actuator/metrics` - Spring Actuator metrics endpoint

## Request Format

```json
{
  "user_prompt": "Show me total sales by customer",
  "tables": [
    {
      "tableName": "sales"
    }
  ]
}
```

## Response Format

```json
{
  "sql": "SELECT customer_id, SUM(sales_amount) as total_sales FROM sales GROUP BY customer_id",
  "success": true,
  "message": "SQL generated successfully"
}
```

## Running the Service

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- Valid OpenAI API key

### Local Development

1. Set your OpenAI API key:
   ```bash
   export OPENAI_API_KEY='your-api-key-here'
   ```

2. Run the service:
   ```bash
   mvn spring-boot:run
   ```

3. Or build and run:
   ```bash
   mvn clean package
   java -jar target/sql-generator-service-1.0.0.jar
   ```

### Testing

Run the tests:
```bash
mvn test
```

Run specific tests:
```bash
mvn test -Dtest=SqlGeneratorControllerTest
mvn test -Dtest=SqlGeneratorServiceIntegrationTest
```

## Service Architecture

- **Controller Layer**: REST endpoints for SQL generation
- **Service Layer**: Business logic for SQL generation using OpenAI
- **DTO Layer**: Data transfer objects for requests and responses
- **Configuration**: OpenAI service configuration and initialization

## Dependencies

- Spring Boot 3.2.x
- Spring Web
- Spring Validation
- Spring Actuator
- OpenAI GPT Java Client
- Jackson for JSON processing
- SLF4J for logging

## Ports

- **8080**: Main application port
- **8081**: Management/Actuator port

## Health Checks

The service includes comprehensive health checks:
- Application health: `/api/sql-generator/health`
- Spring Actuator health: `/actuator/health`

## Error Handling

The service provides detailed error messages for common issues:
- OpenAI API quota exceeded
- Authentication failures
- Rate limiting
- Timeout errors
- Configuration issues
