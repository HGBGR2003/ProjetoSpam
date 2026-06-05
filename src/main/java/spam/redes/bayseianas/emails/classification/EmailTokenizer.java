package spam.redes.bayseianas.emails.classification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import spam.redes.bayseianas.emails.Emails.EmailCleanerService;

/**
 * Tokenização compartilhada entre treinamento e classificação.
 */
@Component
@RequiredArgsConstructor
public class EmailTokenizer {

    private static final int MAX_TOKEN_LENGTH = 100;

    private final EmailCleanerService cleanerService;

    public List<String> tokenizeFromRaw(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return Collections.emptyList();
        }
        String cleaned = cleanerService.clean(rawText);
        return tokenizeCleaned(cleaned);
    }

    public List<String> tokenizeCleaned(String cleanedText) {
        if (cleanedText == null || cleanedText.isBlank()) {
            return Collections.emptyList();
        }

        String[] tokens = cleanedText.split("\\s+");
        List<String> result = new ArrayList<>(tokens.length);

        for (String token : tokens) {
            if (!token.isEmpty() && token.length() <= MAX_TOKEN_LENGTH) {
                result.add(token);
            }
        }

        return result;
    }
}
