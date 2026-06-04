package spam.redes.bayseianas.emails.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositório JPA para {@link ModelMetadataEntity}.
 * A tabela é tratada como singleton: somente uma linha deve existir por vez.
 */
public interface ModelMetadataRepository extends JpaRepository<ModelMetadataEntity, Long> {

    /**
     * Retorna o registro de metadados mais recente (o treinamento mais recente).
     */
    Optional<ModelMetadataEntity> findTopByOrderByTrainedAtDesc();
}
