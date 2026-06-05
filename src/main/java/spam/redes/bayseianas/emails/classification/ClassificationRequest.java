package spam.redes.bayseianas.emails.classification;

import com.fasterxml.jackson.annotation.JsonAlias;

public record ClassificationRequest(
        @JsonAlias("texto")
        String text
) {}
