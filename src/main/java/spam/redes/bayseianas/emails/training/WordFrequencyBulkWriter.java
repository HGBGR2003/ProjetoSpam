package spam.redes.bayseianas.emails.training;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WordFrequencyBulkWriter {

    private static final String INSERT_SQL =
            "INSERT INTO word_frequencies (word, spam_count, ham_count) VALUES (?, ?, ?)";

    private final JdbcTemplate jdbcTemplate;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertBatch(List<WordCountRow> batch) {
        if (batch.isEmpty()) {
            return;
        }
        jdbcTemplate.batchUpdate(
                INSERT_SQL,
                batch,
                batch.size(),
                (ps, row) -> {
                    ps.setString(1, row.word());
                    ps.setInt(2, row.spamCount());
                    ps.setInt(3, row.hamCount());
                });
    }

    public record WordCountRow(String word, int spamCount, int hamCount) {}
}
