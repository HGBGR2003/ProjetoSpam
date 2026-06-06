package spam.redes.bayseianas.emails.classification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import spam.redes.bayseianas.emails.model.ModelMetadataEntity;
import spam.redes.bayseianas.emails.model.ModelMetadataRepository;
import spam.redes.bayseianas.emails.model.WordFrequencyEntity;
import spam.redes.bayseianas.emails.model.WordFrequencyRepository;

/**
 * Classificador Naive Bayes com suavização de Laplace e threshold ajustável.
 *
 * <h3>Por que prior balanceado (0.5/0.5)?</h3>
 * <p>A base de treinamento possui ~600.000 spam para ~2.000 ham (razão 300:1).
 * Usar os priors empíricos faria com que qualquer e-mail fosse classificado como
 * spam apenas pelo desequilíbrio de volume — mesmo e-mails claramente legítimos.
 * O prior balanceado elimina esse viés e deixa as <em>likelihoods</em> das palavras
 * serem os únicos fatores de discriminação.
 *
 * <h3>Por que threshold?</h3>
 * <p>Com prior 0.5/0.5 o ponto de corte natural é 50%. O threshold configurável
 * ({@value #SPAM_THRESHOLD_PROPERTY}, padrão {@value #DEFAULT_SPAM_THRESHOLD})
 * permite elevar essa barreira sem recompilar — útil para reduzir falsos positivos
 * em produção conforme feedback dos usuários.
 *
 * <p>Exemplo: {@code classifier.spam-threshold=0.75} só classifica como spam quando
 * a probabilidade calculada supera 75%.
 */
@Service
public class NaiveBayesClassifierService {

    private static final Logger log = LoggerFactory.getLogger(NaiveBayesClassifierService.class);
    private static final int    IN_QUERY_CHUNK_SIZE = 2_000;

    static final String SPAM_THRESHOLD_PROPERTY = "classifier.spam-threshold";
    static final double DEFAULT_SPAM_THRESHOLD   = 0.75;

    /**
     * Limiar mínimo de P(spam) para classificar um e-mail como spam.
     * Configurável em {@code application.properties} via {@value #SPAM_THRESHOLD_PROPERTY}.
     * Valor padrão: {@value #DEFAULT_SPAM_THRESHOLD}.
     */
    @Value("${" + SPAM_THRESHOLD_PROPERTY + ":" + DEFAULT_SPAM_THRESHOLD + "}")
    private double spamThreshold;

    private final ModelMetadataRepository modelMetadataRepository;
    private final WordFrequencyRepository  wordFrequencyRepository;
    private final EmailTokenizer           emailTokenizer;

    public NaiveBayesClassifierService(
            ModelMetadataRepository modelMetadataRepository,
            WordFrequencyRepository wordFrequencyRepository,
            EmailTokenizer emailTokenizer) {
        this.modelMetadataRepository = modelMetadataRepository;
        this.wordFrequencyRepository  = wordFrequencyRepository;
        this.emailTokenizer           = emailTokenizer;
    }

    public ClassificationResponse classify(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            throw new InvalidClassificationTextException();
        }

        ModelMetadataEntity metadata = modelMetadataRepository.findTopByOrderByTrainedAtDesc()
                .orElseThrow(ModelNotTrainedException::new);

        List<String> tokens = emailTokenizer.tokenizeFromRaw(rawText);

        long totalSpamWords  = metadata.getTotalSpamWords();
        long totalHamWords   = metadata.getTotalHamWords();
        long vocabularySize  = metadata.getVocabularySize();

        // Prior balanceado: ignora a desproporção de volume da base de treinamento.
        // As likelihoods das palavras (Laplace) são o único fator de discriminação.
        double logScoreSpam = Math.log(0.5);
        double logScoreHam  = Math.log(0.5);

        if (tokens.isEmpty()) {
            log.debug("Texto sem tokens após limpeza; usando apenas priors.");
            double[] probs = LogProbabilityCalculator.normalizeLogScores(logScoreSpam, logScoreHam);
            return buildResponse(probs[0], probs[1]);
        }

        Map<String, WordFrequencyEntity> frequencies = loadFrequencies(tokens);

        for (String token : tokens) {
            WordFrequencyEntity freq = frequencies.get(token);
            long spamCount = freq != null ? freq.getSpamCount() : 0L;
            long hamCount  = freq != null ? freq.getHamCount()  : 0L;

            double pSpam = LogProbabilityCalculator.laplaceProbability(
                    spamCount, totalSpamWords, vocabularySize);
            double pHam  = LogProbabilityCalculator.laplaceProbability(
                    hamCount,  totalHamWords,  vocabularySize);

            logScoreSpam += Math.log(pSpam);
            logScoreHam  += Math.log(pHam);
        }

        double[] probs = LogProbabilityCalculator.normalizeLogScores(logScoreSpam, logScoreHam);
        return buildResponse(probs[0], probs[1]);
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

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

    /**
     * Aplica o threshold configurável para decidir a classe final.
     * Um e-mail só é marcado como SPAM se P(spam) >= {@link #spamThreshold}.
     */
    private ClassificationResponse buildResponse(double pSpam, double pHam) {
        String classe = pSpam >= spamThreshold ? "SPAM" : "NÃO SPAM (HAM)";
        log.debug("Classificação: {} | P(spam)={} P(ham)={} threshold={}",
                classe, pSpam, pHam, spamThreshold);
        return new ClassificationResponse(
                classe,
                roundProbability(pSpam),
                roundProbability(pHam));
    }

    private static double roundProbability(double value) {
        return Math.round(value * 1_000_000.0) / 1_000_000.0;
    }
}
