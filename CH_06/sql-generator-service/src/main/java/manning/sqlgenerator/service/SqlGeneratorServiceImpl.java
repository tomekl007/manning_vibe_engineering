package manning.sqlgenerator.service;

import manning.sqlgenerator.dto.SqlGeneratorQueryRequest;
import manning.sqlgenerator.dto.SqlGeneratorQueryResponse;
import manning.sqlgenerator.dto.Table;
import manning.sqlgenerator.service.SqlGeneratorService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class SqlGeneratorServiceImpl implements SqlGeneratorService {

  private static final Logger logger = LoggerFactory.getLogger(SqlGeneratorServiceImpl.class);

  @Value("${openai.api.key}")
  private String openaiApiKey;

  @Value("${openai.model:gpt-4o-mini}")
  private String openaiModel;

  @Value("${openai.timeout:60}")
  private int openaiTimeout;

  private OpenAiService openAiService;

  /**
   * Initialize the OpenAI service when the bean is created.
   */
  public void init() {
    if (openaiApiKey != null && !openaiApiKey.trim().isEmpty()) {
      this.openAiService = new OpenAiService(openaiApiKey, Duration.ofSeconds(openaiTimeout));
      logger.info("OpenAI service initialized successfully");
    } else {
      logger.warn("OpenAI API key not configured, service will not function properly");
    }
  }

  @Override
  public SqlGeneratorQueryResponse generateSql(SqlGeneratorQueryRequest request) {
    try {
      logger.info("Processing SQL generation request");

      if (openAiService == null) {
        logger.error("OpenAI service not initialized");
        return SqlGeneratorQueryResponse.error("Service not properly configured");
      }

      // Build the prompt for OpenAI
      String prompt = buildPrompt(request);

      // Generate SQL using OpenAI
      String generatedSql = generateSqlWithOpenAI(prompt);

      if (generatedSql != null && !generatedSql.trim().isEmpty()) {
        logger.info("Successfully generated SQL");
        return SqlGeneratorQueryResponse.success(generatedSql);
      } else {
        logger.warn("OpenAI returned empty SQL");
        return SqlGeneratorQueryResponse.error("Failed to generate SQL - empty response from AI service");
      }

    } catch (Exception e) {
      logger.error("Error processing SQL generation request", e);
      return SqlGeneratorQueryResponse.error("Internal server error: " + e.getMessage());
    }
  }

  /**
   * Builds a comprehensive prompt for OpenAI based on the request.
   */
  String buildPrompt(SqlGeneratorQueryRequest request) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("You are a SQL expert. Generate a SQL query based on the following information:\n\n");
    prompt.append("User Request: ").append(request.getUserPrompt()).append("\n\n");
    prompt.append("Available Tables:\n");

    for (Table table : request.getTables()) {
      prompt.append("- ").append(table.getTableName()).append("\n");
    }

    prompt.append("\nPlease generate a valid SQL query that addresses the user's request. ");
    prompt.append("Only return the SQL query without any additional explanation or formatting.");

    return prompt.toString();
  }

  /**
   * Generates SQL using OpenAI's chat completion API.
   */
  private String generateSqlWithOpenAI(String prompt) {
    try {
      ChatMessage userMessage = new ChatMessage("user", prompt);

      ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
          .model(openaiModel)
          .messages(List.of(userMessage))
          .maxTokens(500)
          .temperature(0.1)
          .build();

      String response = openAiService.createChatCompletion(completionRequest)
          .getChoices().get(0).getMessage().getContent();

      // Clean up the response - remove any markdown formatting or extra text
      return cleanSqlResponse(response);

    } catch (Exception e) {
      logger.error("Error calling OpenAI API", e);
      throw new RuntimeException("Failed to generate SQL with OpenAI", e);
    }
  }

  /**
   * Cleans up the SQL response from OpenAI to extract just the SQL query.
   */
  String cleanSqlResponse(String response) {
    if (response == null) {
      return null;
    }
    // Remove markdown code blocks
    return response.replaceAll("```sql", "").replaceAll("```", "").trim();
  }
}
