# SQL Generator Service

A standalone Spring Boot service that provides SQL generation functionality for the `generateSql` feature.

## Configuration

The service uses a simplified configuration approach:

- **`application.yml`** - Main configuration for all environments
- **`application-test.yml`** - Test-specific configuration (uses random ports, debug logging)

### Environment Variables

Configure the service via environment variables:

```bash
# Core
export AI_PROVIDER='openai'               # Options: openai, gemini; defaults to openai

# OpenAI (only required when AI_PROVIDER=openai)
export OPENAI_API_KEY='your-openai-api-key'
export OPENAI_MODEL='gpt-4o-mini'         # Optional override
export OPENAI_TIMEOUT='60'                # Optional override

# Google Gemini (only required when AI_PROVIDER=gemini)
export GOOGLE_AI_API_KEY='your-gemini-api-key'
export GOOGLE_AI_MODEL='gemini-2.5-flash' # Optional override
export GOOGLE_AI_TIMEOUT='60'             # Optional override
```

### Choosing an AI Provider

Set `AI_PROVIDER` to either `openai` or `gemini`. Only the selected provider needs to have credentials configured. If the chosen provider is missing required configuration, the service returns an informative error response instead of attempting a request.

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
- API access for the provider you plan to use (OpenAI and/or Google Gemini)

### Local Development

1. Set your preferred provider and credentials:
   ```bash
   export AI_PROVIDER='gemini'
   export GOOGLE_AI_API_KEY='your-api-key'
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
- **Service Layer**: Business logic for SQL generation using pluggable AI providers
- **DTO Layer**: Data transfer objects for requests and responses
- **AI Clients**: Individual clients for OpenAI and Google Gemini with shared contract

## Dependencies

- Spring Boot 3.2.x
- Spring Web
- Spring Validation
- Spring Actuator
- OpenAI GPT Java Client
- Google GenAI Java SDK (Gemini)
- Jackson for JSON processing
- SLF4J for logging

## Deploying to Google Cloud Run

1. **Build the container image**  
   ```bash
   gcloud builds submit --tag gcr.io/PROJECT_ID/sql-generator-service
   ```

2. **Deploy to Cloud Run**  
   ```bash
   gcloud run deploy sql-generator-service \
     --image gcr.io/PROJECT_ID/sql-generator-service \
     --platform managed \
     --region REGION \
     --allow-unauthenticated \
     --set-env-vars=AI_PROVIDER=gemini,GOOGLE_AI_API_KEY=your-gemini-key
   ```

3. **(Optional) Switch providers**  
   Update the `AI_PROVIDER` and credentials via Cloud Run service variables:
   ```bash
   gcloud run services update sql-generator-service \
     --set-env-vars=AI_PROVIDER=openai,OPENAI_API_KEY=your-openai-key
   ```

Cloud Run automatically provides the HTTP port via the `PORT` environment variable. The application uses `${PORT:8080}`, so no additional configuration is required.

### Viewing Logs

To see logs of the deployed application:

**Using gcloud CLI:**
```bash
gcloud beta run services logs tail sql-generator-service --region=europe-west1
```

**Using Google Cloud Console:**
Visit the [Cloud Logging Console](https://console.cloud.google.com/logs/query) and filter for your service, or use a direct query URL (replace `PROJECT_ID` with your Google Cloud project ID):

```
https://console.cloud.google.com/logs/query;cursorTimestamp=2025-11-13T15:17:58.124062Z;duration=PT15M?referrer=search&project=PROJECT_ID
```

**Note**: Replace `europe-west1` with your deployment region and `PROJECT_ID` with your Google Cloud project ID.

## Querying the Deployed API

The deployed service is available at: `https://sql-generator-service-466645099438.europe-west1.run.app`

**Note**: Replace `https://sql-generator-service-466645099438.europe-west1.run.app` with your own deployment URL in the curl commands below.

### Health Check
```bash
curl -X GET https://sql-generator-service-466645099438.europe-west1.run.app/api/sql-generator/health
```

### Service Info
```bash
curl -X GET https://sql-generator-service-466645099438.europe-west1.run.app/api/sql-generator/info
```

### Generate SQL (Basic Example)
```bash
curl -X POST https://sql-generator-service-466645099438.europe-west1.run.app/api/sql-generator/generate-sql \
  -H "Content-Type: application/json" \
  -d '{
    "user_prompt": "Show me total sales by customer",
    "tables": [
      {
        "tableName": "sales"
      },
      {
        "tableName": "customers"
      }
    ]
  }'
```

### Generate SQL (With CREATE TABLE SQL)
```bash
curl -X POST https://sql-generator-service-466645099438.europe-west1.run.app/api/sql-generator/generate-sql \
  -H "Content-Type: application/json" \
  -d '{
    "user_prompt": "Find all customers who made purchases in the last month",
    "tables": [
      {
        "tableName": "customers",
        "createTableSql": "CREATE TABLE customers (id INT PRIMARY KEY, name VARCHAR(100), email VARCHAR(100));"
      },
      {
        "tableName": "orders",
        "createTableSql": "CREATE TABLE orders (id INT PRIMARY KEY, customer_id INT, order_date DATE, amount DECIMAL(10,2));"
      }
    ]
  }'
```

### Generate SQL (With Previous Queries Context)
```bash
curl -X POST https://sql-generator-service-466645099438.europe-west1.run.app/api/sql-generator/generate-sql \
  -H "Content-Type: application/json" \
  -d '{
    "user_prompt": "Show me the same data but only for active customers",
    "tables": [
      {
        "tableName": "customers",
        "createTableSql": "CREATE TABLE customers (id INT PRIMARY KEY, name VARCHAR(100), email VARCHAR(100), status VARCHAR(20));"
      }
    ],
    "n_last_queries": [
      "SELECT id, name, email FROM customers",
      "SELECT * FROM customers WHERE email LIKE '\''%@gmail.com'\''"
    ]
  }'
```

**Note**: The `n_last_queries` field is optional and provides context from previous SQL queries to improve generation accuracy.

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
