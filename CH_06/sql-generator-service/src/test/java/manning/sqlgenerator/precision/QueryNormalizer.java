package manning.sqlgenerator.precision;

/**
 * Normalizes SQL queries for comparison in precision testing.
 */
public class QueryNormalizer {

    /**
     * Normalizes a SQL query by standardizing formatting.
     * This method focuses on standardizing whitespace and quotes.
     *
     * @param query The SQL query to normalize
     * @return The normalized SQL query
     */
    public static String normalize(String query) {
        if (query == null || query.trim().isEmpty()) {
            return query;
        }

        String normalized = query.trim();

        // Standardize whitespace
        normalized = normalized.replaceAll("\\s+", " ");

        // Remove extra spaces around operators
        normalized = normalized.replaceAll("\\s*([=<>!+\\-*/])\\s*", "$1");

        // Standardize quotes (convert backticks to double quotes)
        normalized = normalized.replaceAll("`", "\"");

        return normalized.trim();
    }
}
