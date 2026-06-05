package spam.redes.bayseianas.emails.training;

import java.time.Instant;
import java.util.UUID;

public record TrainingJobResponse(
        UUID jobId,
        TrainingJobStatus status,
        int progressPercent,
        Instant startedAt,
        Instant finishedAt,
        TrainingSummary summary,
        String errorMessage
) {
    public static TrainingJobResponse from(TrainingJob job) {
        return new TrainingJobResponse(
                job.getJobId(),
                job.getStatus(),
                job.getProgressPercent(),
                job.getStartedAt(),
                job.getFinishedAt(),
                job.getSummary(),
                job.getErrorMessage());
    }
}
