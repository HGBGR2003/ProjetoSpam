package spam.redes.bayseianas.emails.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "model_metadata")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelMetadataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long totalSpamEmails;

    @Column(nullable = false)
    private Long totalHamEmails;

    @Column(nullable = false)
    private Long totalSpamWords;

    @Column(nullable = false)
    private Long totalHamWords;

    @Column(nullable = false)
    private Long vocabularySize;

    @Column(nullable = false)
    private LocalDateTime trainedAt;
}
