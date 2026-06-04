package spam.redes.bayseianas.emails.training;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * Expõe o endpoint para acionar manualmente o treinamento do modelo Naive Bayes.
 *
 * <pre>
 *   POST /api/model/train
 * </pre>
 */
@RestController
@RequestMapping("/api/model")
@RequiredArgsConstructor
public class TrainingController {

    private final NaiveBayesTrainingService trainingService;

    /**
     * Dispara o treinamento completo do modelo e retorna um sumário com as estatísticas.
     *
     * <p>Exemplo de resposta:
     * <pre>
     * {
     *   "totalSpamEmails": 4827,
     *   "totalHamEmails":  2551,
     *   "totalSpamWords":  812345,
     *   "totalHamWords":   430210,
     *   "vocabularySize":  47813,
     *   "elapsedTimeMs":   3210
     * }
     * </pre>
     *
     * @return 200 OK com o {@link TrainingSummary} serializado em JSON.
     */
    @PostMapping("/train")
    public ResponseEntity<TrainingSummary> train() {
        TrainingSummary summary = trainingService.trainModel();
        return ResponseEntity.ok(summary);
    }
}
