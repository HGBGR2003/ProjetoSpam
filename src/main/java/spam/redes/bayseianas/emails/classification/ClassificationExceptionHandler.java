package spam.redes.bayseianas.emails.classification;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = ClassificationController.class)
public class ClassificationExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ClassificationExceptionHandler.class);

    @ExceptionHandler(InvalidClassificationTextException.class)
    public ResponseEntity<Map<String, String>> handleInvalidText(
            InvalidClassificationTextException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(ModelNotTrainedException.class)
    public ResponseEntity<Map<String, String>> handleModelNotTrained(
            ModelNotTrainedException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpected(Exception ex) {
        log.error("Erro inesperado na classificação", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Erro interno ao classificar o e-mail."));
    }
}
