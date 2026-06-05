package spam.redes.bayseianas.emails.training;

import java.time.Instant;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrainingJob {

    private final UUID jobId;
    private TrainingJobStatus status;
    private Instant startedAt;
    private Instant finishedAt;
    private int progressPercent;
    private TrainingSummary summary;
    private String errorMessage;

    public TrainingJob(UUID jobId) {
        this.jobId = jobId;
        this.status = TrainingJobStatus.PENDING;
        this.progressPercent = 0;
    }
}
