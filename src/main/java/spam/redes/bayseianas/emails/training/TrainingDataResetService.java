package spam.redes.bayseianas.emails.training;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import spam.redes.bayseianas.emails.model.ModelMetadataRepository;
import spam.redes.bayseianas.emails.model.WordFrequencyRepository;

@Service
@RequiredArgsConstructor
public class TrainingDataResetService {

    private final WordFrequencyRepository wordFrequencyRepository;
    private final ModelMetadataRepository modelMetadataRepository;

    @Transactional
    public void resetPreviousTraining() {
        wordFrequencyRepository.truncate();
        modelMetadataRepository.deleteAll();
    }
}
