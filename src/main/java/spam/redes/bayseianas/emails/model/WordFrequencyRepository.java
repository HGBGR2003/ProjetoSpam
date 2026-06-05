package spam.redes.bayseianas.emails.model;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * Repositório JPA para {@link WordFrequencyEntity}.
 */
public interface WordFrequencyRepository extends JpaRepository<WordFrequencyEntity, Long> {

    /**
     * Remove todos os registros da tabela de forma eficiente via TRUNCATE,
     * utilizado antes de um re-treinamento para evitar dados obsoletos.
     */
    @Modifying
    @Query(value = "TRUNCATE TABLE word_frequencies RESTART IDENTITY", nativeQuery = true)
    void truncate();

    List<WordFrequencyEntity> findByWordIn(Collection<String> words);
}
