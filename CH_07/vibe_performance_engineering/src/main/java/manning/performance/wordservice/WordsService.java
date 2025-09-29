package manning.performance.wordservice;

public interface WordsService {
    String getWordOfTheDay();

    boolean wordExists(String word);
}
