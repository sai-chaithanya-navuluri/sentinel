package dev.sentinel.matching;

import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class TextNormalizer {

    // Numbers, timestamps, and UUIDs vary between occurrences of the SAME
    // problem and should not affect whether two incidents are considered similar.
    private static final Pattern NUMBERS = Pattern.compile("\\d+");
    private static final Pattern PUNCTUATION = Pattern.compile("[^a-z0-9\\s]");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    // Common words that carry no distinguishing signal for incident matching.
    private static final Set<String> STOPWORDS = Set.of(
            "a", "an", "the", "is", "was", "were", "be", "been", "being",
            "to", "of", "in", "on", "at", "by", "for", "with", "after", "during"
    );

    /**
     * Produces a normalized set of significant tokens from incident text.
     * Two texts describing the same problem should produce overlapping token sets
     * even if their exact wording differs.
     */
    public Set<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return Set.of();
        }

        String cleaned = text.toLowerCase();
        cleaned = NUMBERS.matcher(cleaned).replaceAll(" ");
        cleaned = PUNCTUATION.matcher(cleaned).replaceAll(" ");
        cleaned = WHITESPACE.matcher(cleaned).replaceAll(" ").trim();

        Set<String> tokens = new LinkedHashSet<>();
        for (String word : cleaned.split(" ")) {
            if (!word.isBlank() && !STOPWORDS.contains(word) && word.length() > 1) {
                tokens.add(word);
            }
        }
        return tokens;
    }
}