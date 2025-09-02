package manning.sqlgenerator.precision;

/**
 * Calculates similarity between SQL queries using Levenshtein distance.
 */
public class LevenshteinSimilarityCalculator {

    /**
     * Calculates the similarity between two SQL queries using Levenshtein distance.
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

        int distance = calculateLevenshteinDistance(query1, query2);
        int maxLength = Math.max(query1.length(), query2.length());

        if (maxLength == 0) {
            return 1.0;
        }

        return 1.0 - ((double) distance / maxLength);
    }

    /**
     * Calculates the Levenshtein distance between two strings.
     */
    private static int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j], Math.min(dp[i][j - 1], dp[i - 1][j - 1]));
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }
}
