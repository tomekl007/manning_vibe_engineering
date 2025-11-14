package manning.sqlgenerator.service.llm;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Google Gemini-backed implementation of {@link LanguageModelClient}.
 * Uses the official Google Gen AI Java SDK.
 */
@Component
public class GeminiLanguageModelClient implements LanguageModelClient {

    private static final Logger logger = LoggerFactory.getLogger(GeminiLanguageModelClient.class);

    private final String apiKey;
    private final String model;

    private Client genAiClient;

    public GeminiLanguageModelClient(
            @Value("${google.ai.api.key:}") String apiKey,
            @Value("${google.ai.model:gemini-2.5-flash}") String model) {
        this.apiKey = apiKey;
        this.model = model;
    }

    @PostConstruct
    void init() {
        if (StringUtils.hasText(apiKey)) {
            // Explicitly set API key for Gemini Developer API backend.
            this.genAiClient = Client.builder()
                    .apiKey(apiKey)
                    .build();
            logger.info("Gemini GenAI client initialized successfully using model {}", model);
        } else {
            logger.info("Google AI API key not configured; Gemini client disabled");
        }
    }

    @Override
    public String getProviderName() {
        return "gemini";
    }

    @Override
    public boolean isEnabled() {
        return genAiClient != null;
    }

    @Override
    public String generateText(String prompt) {
        if (!isEnabled()) {
            throw new IllegalStateException("Gemini client is not enabled");
        }

        try {
            // Simple text-only call using the convenience overload
            GenerateContentResponse response =
                    genAiClient.models.generateContent(model, prompt, null);

            String text = response.text();

            if (!StringUtils.hasText(text)) {
                throw new RuntimeException("Gemini response contained empty text");
            }

            return text;
        } catch (Exception e) {
            logger.error("Error calling Google Gemini API", e);
            throw new RuntimeException("Failed to generate SQL with Gemini", e);
        }
    }
}
