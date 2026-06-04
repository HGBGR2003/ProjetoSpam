package spam.redes.bayseianas.emails.model;

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
 * Armazena a frequência de cada palavra do vocabulário,
 * discriminada por classe (spam vs. ham).
 * Populada pelo NaiveBayesTrainingService durante o treinamento.
 */
@Entity
@Table(name = "word_frequencies")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WordFrequencyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Palavra única do vocabulário (normalizada/limpa). */
    @Column(nullable = false, unique = true, length = 100)
    private String word;

    /** Quantas vezes esta palavra apareceu em e-mails classificados como spam. */
    @Column(nullable = false)
    private Integer spamCount;

    /** Quantas vezes esta palavra apareceu em e-mails classificados como ham. */
    @Column(nullable = false)
    private Integer hamCount;
}
