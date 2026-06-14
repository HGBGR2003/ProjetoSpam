package spam.redes.bayseianas.emails.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ModelMetadataRepository extends JpaRepository<ModelMetadataEntity, Long> {

    Optional<ModelMetadataEntity> findTopByOrderByTrainedAtDesc();
}
