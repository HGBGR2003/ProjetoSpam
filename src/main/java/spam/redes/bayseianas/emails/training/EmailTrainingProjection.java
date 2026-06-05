package spam.redes.bayseianas.emails.training;

/**
 * Projeção leve para leitura em lotes durante o treinamento (evita carregar entidade completa).
 */
public interface EmailTrainingProjection {

    Long getId();

    String getContent();

    Boolean getIsSpam();
}
