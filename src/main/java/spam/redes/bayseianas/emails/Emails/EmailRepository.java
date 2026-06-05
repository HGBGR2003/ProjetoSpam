package spam.redes.bayseianas.emails.Emails;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import spam.redes.bayseianas.emails.training.EmailTrainingProjection;

public interface EmailRepository extends JpaRepository<EmailEntity, Long> {

    boolean existsBySourceFile(String sourceFile);

    @Query("""
            SELECT e.id AS id, e.content AS content, e.isSpam AS isSpam
            FROM EmailEntity e
            WHERE e.id > :lastId
            ORDER BY e.id ASC
            """)
    List<EmailTrainingProjection> findTrainingBatch(
            @Param("lastId") long lastId,
            Pageable pageable);
}
