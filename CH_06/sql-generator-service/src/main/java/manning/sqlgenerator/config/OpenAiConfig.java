package manning.sqlgenerator.config;

import manning.sqlgenerator.service.impl.SqlGeneratorServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

@Configuration
public class OpenAiConfig {

    @Autowired
    private SqlGeneratorServiceImpl sqlGeneratorService;

    @PostConstruct
    public void initOpenAiService() {
        sqlGeneratorService.init();
    }
}
