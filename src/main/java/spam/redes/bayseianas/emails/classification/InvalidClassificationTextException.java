package spam.redes.bayseianas.emails.classification;

public class InvalidClassificationTextException extends RuntimeException {

    public InvalidClassificationTextException() {
        super("Texto obrigatório para classificação.");
    }
}
