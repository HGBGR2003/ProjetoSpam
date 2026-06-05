package spam.redes.bayseianas.emails.training;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrainingJobService {

    private final TrainingJobRegistry jobRegistry;
    private final TrainingAsyncExecutor trainingAsyncExecutor;

    public TrainingJob startTraining() {
        if (jobRegistry.hasRunningJob()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe um treinamento em execução. Consulte GET /api/model/train/latest");
        }

        TrainingJob job = jobRegistry.createJob();
        trainingAsyncExecutor.run(job.getJobId());
        return job;
    }
}
