package spam.redes.bayseianas.emails.training;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TrainingAsyncExecutor {

    private static final Logger log = LoggerFactory.getLogger(TrainingAsyncExecutor.class);

    private final NaiveBayesTrainingService trainingService;
    private final TrainingJobRegistry jobRegistry;

    @Async("trainingExecutor")
    public void run(UUID jobId) {
        try {
            TrainingSummary summary = trainingService.trainModel(
                    jobId,
                    (currentBatch, totalBatches, progressPercent) ->
                            jobRegistry.updateProgress(jobId, progressPercent));

            jobRegistry.complete(jobId, summary);
            log.info("Treinamento {} concluído com sucesso.", jobId);
        } catch (Exception e) {
            log.error("Treinamento {} falhou: {}", jobId, e.getMessage(), e);
            jobRegistry.fail(jobId, e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
        }
    }
}
