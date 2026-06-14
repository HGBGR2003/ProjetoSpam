package spam.redes.bayseianas.emails.model;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface WordFrequencyRepository extends JpaRepository<WordFrequencyEntity, Long> {

    @Modifying
    @Query(value = "TRUNCATE TABLE word_frequencies RESTART IDENTITY", nativeQuery = true)
    void truncate();

    List<WordFrequencyEntity> findByWordIn(Collection<String> words);
}
