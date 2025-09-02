package manning.sqlgenerator.precision;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Calculates similarity between SQL queries using word overlap.
 */
public class OverlapSimilarityCalculator {

    /**
     * Calculates the similarity between two SQL queries using word overlap.
     *
     * @param query1 First SQL query
     * @param query2 Second SQL query
     * @return Similarity score between 0 and 1 (1 = identical)
     */
    public static double calculateQuerySimilarity(String query1, String query2) {
        if (query1 == null || query2 == null) {
            return 0.0;
        }

        if (query1.equals(query2)) {
            return 1.0;
        }

        Set<String> words1 = extractWords(query1);
        Set<String> words2 = extractWords(query2);

        if (words1.isEmpty() && words2.isEmpty()) {
            return 1.0;
        }

        if (words1.isEmpty() || words2.isEmpty()) {
            return 0.0;
        }

        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);

        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);

        return (double) intersection.size() / union.size();
    }

    /**
     * Extracts words from a SQL query, filtering out common SQL keywords and symbols.
     */
    private static Set<String> extractWords(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new HashSet<>();
        }

        // Remove SQL keywords and common symbols, keep only meaningful words
        String cleaned = query.toLowerCase()
            .replaceAll("\\b(select|from|where|and|or|order by|group by|having|limit|offset|as|on|join|inner|left|right|outer|union|all|distinct|count|sum|avg|min|max|case|when|then|else|end|in|not|like|between|is|null|true|false|cast|as|real|int|varchar|text|date|timestamp)\\b", " ")
            .replaceAll("[^a-zA-Z0-9_]", " ")
            .replaceAll("\\s+", " ")
            .trim();

        if (cleaned.isEmpty()) {
            return new HashSet<>();
        }

        return new HashSet<>(Arrays.asList(cleaned.split("\\s+")));
    }
}
