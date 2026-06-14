package spam.redes.bayseianas.emails.training;

public interface EmailTrainingProjection {

    Long getId();

    String getContent();

    Boolean getIsSpam();
}
