package manning.sqlgenerator.service;

import jakarta.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import manning.sqlgenerator.dto.SqlGeneratorQueryRequest;
import manning.sqlgenerator.dto.SqlGeneratorQueryResponse;
import manning.sqlgenerator.dto.Table;
import manning.sqlgenerator.service.llm.LanguageModelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SqlGeneratorServiceImpl implements SqlGeneratorService {

  private static final Logger logger = LoggerFactory.getLogger(SqlGeneratorServiceImpl.class);

  private final Map<String, LanguageModelClient> clientsByName;
  private final String providerName;

  public SqlGeneratorServiceImpl(
      List<LanguageModelClient> clients,
      @Value("${ai.provider:openai}") String providerName) {
    this.clientsByName =
        clients.stream()
            .collect(
                Collectors.toMap(
                    client -> client.getProviderName().toLowerCase(Locale.ROOT),
                    client -> client,
                    (left, right) -> left,
                    LinkedHashMap::new));
    this.providerName = providerName.toLowerCase(Locale.ROOT);
  }

  @PostConstruct
  void logProviderConfiguration() {
    if (clientsByName.isEmpty()) {
      logger.warn("No language model clients registered. SQL generation will not be possible.");
    } else {
      logger.info(
          "Configured AI providers: {}. Active provider: {}",
          String.join(", ", clientsByName.keySet()),
          providerName);
    }
  }

  @Override
  public SqlGeneratorQueryResponse generateSql(SqlGeneratorQueryRequest request) {
    try {
      logger.info("Processing SQL generation request using provider {}", providerName);

      Optional<LanguageModelClient> selectedClient =
          Optional.ofNullable(clientsByName.get(providerName));

      if (selectedClient.isEmpty()) {
        logger.error("No AI provider registered under key {}", providerName);
        return SqlGeneratorQueryResponse.error(
            "AI provider '%s' is not supported".formatted(providerName));
      }

      LanguageModelClient client = selectedClient.get();
      if (!client.isEnabled()) {
        logger.error("AI provider {} is not properly configured", providerName);
        return SqlGeneratorQueryResponse.error(
            "AI provider '%s' is not configured".formatted(providerName));
      }

      String prompt = buildPrompt(request);
      String rawResponse = client.generateText(prompt);

      String generatedSql = cleanSqlResponse(rawResponse);

      if (StringUtils.hasText(generatedSql)) {
        logger.info("Successfully generated SQL using provider {}", providerName);
        return SqlGeneratorQueryResponse.success(generatedSql);
      } else {
        logger.warn("Provider {} returned empty SQL", providerName);
        return SqlGeneratorQueryResponse.error(
            "Failed to generate SQL - empty response from AI service");
      }

    } catch (Exception e) {
      logger.error("Error processing SQL generation request", e);
      return SqlGeneratorQueryResponse.error("Internal server error: " + e.getMessage());
    }
  }

  /**
   * Builds a comprehensive prompt for an AI model based on the request.
   */
  String buildPrompt(SqlGeneratorQueryRequest request) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("You are a SQL expert. Generate a SQL query based on the following information:\n\n");
    prompt.append("User Request: ").append(request.getUserPrompt()).append("\n\n");
    prompt.append("Available Tables:\n");

    for (Table table : request.getTables()) {
      prompt.append("- ").append(table.getCreateTableSql()).append("\n");
    }

    // Add last N queries if available
    if (request.getNLastQueries() != null && !request.getNLastQueries().isEmpty()) {
      prompt.append("\nRecent Queries for those tables:\n");
      for (int i = 0; i < request.getNLastQueries().size(); i++) {
        prompt.append(i + 1).append(". ").append(request.getNLastQueries().get(i)).append("\n");
      }
      prompt.append("\n");
    }

    prompt.append("Please generate a valid SQL query that addresses the user's request. ");
    prompt.append("Only return the SQL query without any additional explanation or formatting.");

    return prompt.toString();
  }

  /**
   * Cleans up the SQL response from an AI model to extract just the SQL query.
   */
  String cleanSqlResponse(String response) {
    if (response == null) {
      return null;
    }
    // Remove markdown code blocks
    return response.replaceAll("```sql", "").replaceAll("```", "").trim();
  }
}
