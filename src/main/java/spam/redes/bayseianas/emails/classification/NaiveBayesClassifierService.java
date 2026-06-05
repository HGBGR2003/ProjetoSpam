package spam.redes.bayseianas.emails.classification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import spam.redes.bayseianas.emails.model.ModelMetadataEntity;
import spam.redes.bayseianas.emails.model.ModelMetadataRepository;
import spam.redes.bayseianas.emails.model.WordFrequencyEntity;
import spam.redes.bayseianas.emails.model.WordFrequencyRepository;

@Service
@RequiredArgsConstructor
public class NaiveBayesClassifierService {

    private static final Logger log = LoggerFactory.getLogger(NaiveBayesClassifierService.class);
    private static final int IN_QUERY_CHUNK_SIZE = 2_000;

    private final ModelMetadataRepository modelMetadataRepository;
    private final WordFrequencyRepository wordFrequencyRepository;
    private final EmailTokenizer emailTokenizer;

    public ClassificationResponse classify(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            throw new InvalidClassificationTextException();
        }

        ModelMetadataEntity metadata = modelMetadataRepository.findTopByOrderByTrainedAtDesc()
                .orElseThrow(ModelNotTrainedException::new);

        List<String> tokens = emailTokenizer.tokenizeFromRaw(rawText);

        long totalSpamEmails = metadata.getTotalSpamEmails();
        long totalHamEmails = metadata.getTotalHamEmails();
        long totalEmails = totalSpamEmails + totalHamEmails;
        long totalSpamWords = metadata.getTotalSpamWords();
        long totalHamWords = metadata.getTotalHamWords();
        long vocabularySize = metadata.getVocabularySize();

        // double logScoreSpam = LogProbabilityCalculator.logPrior(totalSpamEmails, totalEmails);
        // double logScoreHam = LogProbabilityCalculator.logPrior(totalHamEmails, totalEmails);

        double logScoreSpam = Math.log(0.2);
        double logScoreHam = Math.log(0.8);

        if (tokens.isEmpty()) {
            log.debug("Texto sem tokens após limpeza; usando apenas priors.");
            double[] probs = LogProbabilityCalculator.normalizeLogScores(logScoreSpam, logScoreHam);
            return buildResponse(probs[0], probs[1]);
        }

        Map<String, WordFrequencyEntity> frequencies = loadFrequencies(tokens);

        for (String token : tokens) {
            WordFrequencyEntity freq = frequencies.get(token);
            long spamCount = freq != null ? freq.getSpamCount() : 0L;
            long hamCount = freq != null ? freq.getHamCount() : 0L;

            double pSpam = LogProbabilityCalculator.laplaceProbability(
                    spamCount, totalSpamWords, vocabularySize);
            double pHam = LogProbabilityCalculator.laplaceProbability(
                    hamCount, totalHamWords, vocabularySize);

            logScoreSpam += Math.log(pSpam);
            logScoreHam += Math.log(pHam);
        }

        double[] probs = LogProbabilityCalculator.normalizeLogScores(logScoreSpam, logScoreHam);
        return buildResponse(probs[0], probs[1]);
    }

    private Map<String, WordFrequencyEntity> loadFrequencies(List<String> tokens) {
        Set<String> uniqueTokens = new HashSet<>(tokens);
        Map<String, WordFrequencyEntity> result = new HashMap<>(uniqueTokens.size());

        List<String> tokenList = new ArrayList<>(uniqueTokens);
        for (int i = 0; i < tokenList.size(); i += IN_QUERY_CHUNK_SIZE) {
            int end = Math.min(i + IN_QUERY_CHUNK_SIZE, tokenList.size());
            List<String> chunk = tokenList.subList(i, end);
            for (WordFrequencyEntity entity : wordFrequencyRepository.findByWordIn(chunk)) {
                result.put(entity.getWord(), entity);
            }
        }

        return result;
    }

    private ClassificationResponse buildResponse(double pSpam, double pHam) {
        String classe = pSpam >= pHam ? "SPAM" : "NÃO SPAM (HAM)";
        return new ClassificationResponse(
                classe,
                roundProbability(pSpam),
                roundProbability(pHam));
    }

    private static double roundProbability(double value) {
        return Math.round(value * 1_000_000.0) / 1_000_000.0;
    }
}
