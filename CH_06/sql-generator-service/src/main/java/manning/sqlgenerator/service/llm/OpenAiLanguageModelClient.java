package manning.sqlgenerator.service.llm;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * OpenAI-backed implementation of {@link LanguageModelClient}.
 */
@Component
public class OpenAiLanguageModelClient implements LanguageModelClient {

  private static final Logger logger = LoggerFactory.getLogger(OpenAiLanguageModelClient.class);

  @Value("${openai.api.key:}")
  private String openaiApiKey;

  @Value("${openai.model:gpt-4o-mini}")
  private String openaiModel;

  @Value("${openai.timeout:60}")
  private int openaiTimeout;

  private OpenAiService openAiService;

  @PostConstruct
  public void init() {
    if (openaiApiKey != null && !openaiApiKey.trim().isEmpty()) {
      this.openAiService = new OpenAiService(openaiApiKey, Duration.ofSeconds(openaiTimeout));
      logger.info("OpenAI client initialized successfully using model {}", openaiModel);
    } else {
      logger.info("OpenAI API key not configured; OpenAI client disabled");
    }
  }

  @Override
  public String getProviderName() {
    return "openai";
  }

  @Override
  public boolean isEnabled() {
    return openAiService != null;
  }

  @Override
  public String generateText(String prompt) {
    if (!isEnabled()) {
      throw new IllegalStateException("OpenAI client is not enabled");
    }
    try {
      ChatMessage userMessage = new ChatMessage("user", prompt);

      ChatCompletionRequest completionRequest =
          ChatCompletionRequest.builder()
              .model(openaiModel)
              .messages(List.of(userMessage))
              .maxTokens(500)
              .temperature(0.1)
              .build();

      return openAiService
          .createChatCompletion(completionRequest)
          .getChoices()
          .get(0)
          .getMessage()
          .getContent();
    } catch (Exception e) {
      logger.error("Error calling OpenAI API", e);
      throw new RuntimeException("Failed to generate SQL with OpenAI", e);
    }
  }
}

