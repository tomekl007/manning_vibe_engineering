package manning.performance.wordservice;

import manning.performance.wordservice.traced.TracedWordsController;
import manning.performance.wordservice.optimized.OptimizedWordsController;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;

public class HttpApplication extends Application<Configuration> {

    @Override
    public void run(Configuration configuration, Environment environment) {
        // Original endpoints (baseline performance)
        WordsController wordsController = new WordsController();
        environment.jersey().register(wordsController);
        
        // Traced endpoints for performance analysis
        TracedWordsController tracedWordsController = new TracedWordsController();
        environment.jersey().register(tracedWordsController);
        
        // Optimized endpoints with HashSet caching (90-99% performance improvement)
        OptimizedWordsController optimizedWordsController = new OptimizedWordsController();
        environment.jersey().register(optimizedWordsController);
    }

    // it will be accessible under
    // http://localhost:8080/words
    public static void main(String[] args) throws Exception {
        new HttpApplication().run("server");
    }
}
