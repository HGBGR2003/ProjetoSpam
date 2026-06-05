package spam.redes.bayseianas.emails.training;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class TrainingJobRegistry {

    private final ConcurrentHashMap<UUID, TrainingJob> jobs = new ConcurrentHashMap<>();
    private volatile UUID latestJobId;

    public TrainingJob createJob() {
        UUID jobId = UUID.randomUUID();
        TrainingJob job = new TrainingJob(jobId);
        job.setStatus(TrainingJobStatus.RUNNING);
        job.setStartedAt(Instant.now());
        jobs.put(jobId, job);
        latestJobId = jobId;
        return job;
    }

    public Optional<TrainingJob> find(UUID jobId) {
        return Optional.ofNullable(jobs.get(jobId));
    }

    public Optional<TrainingJob> findLatest() {
        UUID id = latestJobId;
        if (id == null) {
            return Optional.empty();
        }
        return find(id);
    }

    public void updateProgress(UUID jobId, int progressPercent) {
        TrainingJob job = jobs.get(jobId);
        if (job != null) {
            job.setProgressPercent(progressPercent);
        }
    }

    public void complete(UUID jobId, TrainingSummary summary) {
        TrainingJob job = jobs.get(jobId);
        if (job != null) {
            job.setStatus(TrainingJobStatus.COMPLETED);
            job.setProgressPercent(100);
            job.setSummary(summary);
            job.setFinishedAt(Instant.now());
        }
    }

    public void fail(UUID jobId, String errorMessage) {
        TrainingJob job = jobs.get(jobId);
        if (job != null) {
            job.setStatus(TrainingJobStatus.FAILED);
            job.setErrorMessage(errorMessage);
            job.setFinishedAt(Instant.now());
        }
    }

    public boolean hasRunningJob() {
        return jobs.values().stream()
                .anyMatch(j -> j.getStatus() == TrainingJobStatus.RUNNING);
    }
}
