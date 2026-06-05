package spam.redes.bayseianas.emails.training;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

/**
 * Treinamento assíncrono do modelo Naive Bayes.
 *
 * <pre>
 *   POST /api/model/train              → 202 Accepted + jobId
 *   GET  /api/model/train/status/{id}  → progresso e sumário
 *   GET  /api/model/train/latest       → último job disparado
 * </pre>
 */
@RestController
@RequestMapping("/api/model")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingJobService trainingJobService;
    private final TrainingJobRegistry jobRegistry;

    @PostMapping("/train")
    public ResponseEntity<TrainingJobResponse> train() {
        TrainingJob job = trainingJobService.startTraining();
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(TrainingJobResponse.from(job));
    }

    @GetMapping("/train/status/{jobId}")
    public ResponseEntity<TrainingJobResponse> status(@PathVariable UUID jobId) {
        TrainingJob job = jobRegistry.find(jobId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Job de treinamento não encontrado: " + jobId));
        return ResponseEntity.ok(TrainingJobResponse.from(job));
    }

    @GetMapping("/train/latest")
    public ResponseEntity<TrainingJobResponse> latest() {
        TrainingJob job = jobRegistry.findLatest()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Nenhum treinamento foi iniciado ainda."));
        return ResponseEntity.ok(TrainingJobResponse.from(job));
    }
}
