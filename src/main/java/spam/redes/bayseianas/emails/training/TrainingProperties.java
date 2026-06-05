package spam.redes.bayseianas.emails.training;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "training")
@Getter
@Setter
public class TrainingProperties {

    /** E-mails lidos por lote (keyset). */
    private int batchSize = 5_000;

    /** Linhas por batch INSERT JDBC. */
    private int insertBatchSize = 1_000;

    /**
     * Limite de e-mails processados (0 = todos).
     * Útil para testes locais rápidos antes do dataset completo.
     */
    private long maxEmails = 0;
}
