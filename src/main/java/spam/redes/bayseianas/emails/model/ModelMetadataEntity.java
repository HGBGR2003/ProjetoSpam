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

/**
 * Singleton que persiste os totais globais do modelo treinado.
 * Esses valores são os denominadores e priors do Teorema de Bayes
 * com Suavização de Laplace, evitando recálculo a cada classificação.
 *
 * <pre>
 *  P(Spam)  = totalSpamEmails  / (totalSpamEmails + totalHamEmails)
 *  P(w|Spam) = (spamCount(w) + 1) / (totalSpamWords + vocabularySize)   [Laplace]
 * </pre>
 */
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

    /** Total de e-mails de spam usados no treinamento. */
    @Column(nullable = false)
    private Long totalSpamEmails;

    /** Total de e-mails de ham (legítimos) usados no treinamento. */
    @Column(nullable = false)
    private Long totalHamEmails;

    /** Soma de todas as palavras encontradas nos e-mails de spam. */
    @Column(nullable = false)
    private Long totalSpamWords;

    /** Soma de todas as palavras encontradas nos e-mails de ham. */
    @Column(nullable = false)
    private Long totalHamWords;

    /** Número de palavras únicas no vocabulário consolidado. */
    @Column(nullable = false)
    private Long vocabularySize;

    /** Momento em que o treinamento foi concluído. */
    @Column(nullable = false)
    private LocalDateTime trainedAt;
}
