package spam.redes.bayseianas.emails.classification;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ClassificationResponse(
        String classe,
        @JsonProperty("probabilidade_spam") Double probabilidadeSpam,
        @JsonProperty("probabilidade_ham") Double probabilidadeHam
) {}
