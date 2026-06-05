package spam.redes.bayseianas.emails.classification;

public class ModelNotTrainedException extends RuntimeException {

    public ModelNotTrainedException() {
        super("Execute o treinamento antes de classificar e-mails.");
    }
}
