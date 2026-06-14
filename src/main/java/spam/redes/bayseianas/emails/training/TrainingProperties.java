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

    private int batchSize = 5_000;

    private int insertBatchSize = 1_000;

    private long maxEmails = 0;
}
