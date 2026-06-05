package spam.redes.bayseianas.emails.training;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import spam.redes.bayseianas.emails.model.ModelMetadataEntity;
import spam.redes.bayseianas.emails.model.ModelMetadataRepository;

@Service
@RequiredArgsConstructor
public class TrainingMetadataService {

    private final ModelMetadataRepository modelMetadataRepository;

    @Transactional
    public void save(
            long totalSpamEmails,
            long totalHamEmails,
            long totalSpamWords,
            long totalHamWords,
            long vocabularySize) {

        ModelMetadataEntity metadata = ModelMetadataEntity.builder()
                .totalSpamEmails(totalSpamEmails)
                .totalHamEmails(totalHamEmails)
                .totalSpamWords(totalSpamWords)
                .totalHamWords(totalHamWords)
                .vocabularySize(vocabularySize)
                .trainedAt(LocalDateTime.now())
                .build();

        modelMetadataRepository.save(metadata);
    }
}
