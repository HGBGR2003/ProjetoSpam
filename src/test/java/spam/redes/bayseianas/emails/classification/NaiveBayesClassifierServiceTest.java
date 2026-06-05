package spam.redes.bayseianas.emails.classification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import spam.redes.bayseianas.emails.Emails.EmailCleanerService;
import spam.redes.bayseianas.emails.model.ModelMetadataEntity;
import spam.redes.bayseianas.emails.model.ModelMetadataRepository;
import spam.redes.bayseianas.emails.model.WordFrequencyEntity;
import spam.redes.bayseianas.emails.model.WordFrequencyRepository;

@ExtendWith(MockitoExtension.class)
class NaiveBayesClassifierServiceTest {

    @Mock
    private ModelMetadataRepository modelMetadataRepository;

    @Mock
    private WordFrequencyRepository wordFrequencyRepository;

    private NaiveBayesClassifierService classifierService;

    @BeforeEach
    void setUp() {
        EmailCleanerService cleanerService = new EmailCleanerService();
        EmailTokenizer emailTokenizer = new EmailTokenizer(cleanerService);
        classifierService = new NaiveBayesClassifierService(
                modelMetadataRepository, wordFrequencyRepository, emailTokenizer);
    }

    @Test
    void classify_throwsWhenTextIsBlank() {
        assertThrows(InvalidClassificationTextException.class,
                () -> classifierService.classify("   "));
    }

    @Test
    void classify_throwsWhenModelNotTrained() {
        when(modelMetadataRepository.findTopByOrderByTrainedAtDesc()).thenReturn(Optional.empty());
        assertThrows(ModelNotTrainedException.class,
                () -> classifierService.classify("oferta grátis"));
    }

    @Test
    void classify_returnsNormalizedProbabilities() {
        when(modelMetadataRepository.findTopByOrderByTrainedAtDesc())
                .thenReturn(Optional.of(metadata(10, 10, 100, 100, 20)));

        when(wordFrequencyRepository.findByWordIn(anyList()))
                .thenReturn(List.of(
                        WordFrequencyEntity.builder()
                                .word("spamword")
                                .spamCount(8)
                                .hamCount(1)
                                .build()));

        ClassificationResponse response = classifierService.classify("spamword spamword");

        assertEquals(1.0, response.probabilidadeSpam() + response.probabilidadeHam(), 1e-6);
        assertEquals("SPAM", response.classe());
    }

    @Test
    void classify_usesLaplaceForUnknownToken() {
        when(modelMetadataRepository.findTopByOrderByTrainedAtDesc())
                .thenReturn(Optional.of(metadata(5, 5, 50, 50, 10)));

        when(wordFrequencyRepository.findByWordIn(anyList())).thenReturn(List.of());

        ClassificationResponse response = classifierService.classify("novapalavra");

        assertEquals(1.0, response.probabilidadeSpam() + response.probabilidadeHam(), 1e-6);
    }

    private static ModelMetadataEntity metadata(
            long spamEmails, long hamEmails,
            long spamWords, long hamWords, long vocabularySize) {
        return ModelMetadataEntity.builder()
                .totalSpamEmails(spamEmails)
                .totalHamEmails(hamEmails)
                .totalSpamWords(spamWords)
                .totalHamWords(hamWords)
                .vocabularySize(vocabularySize)
                .trainedAt(LocalDateTime.now())
                .build();
    }
}
