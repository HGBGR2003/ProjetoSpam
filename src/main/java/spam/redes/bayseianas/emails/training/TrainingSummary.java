package spam.redes.bayseianas.emails.training;

/**
 * DTO imutável com o sumário retornado após o treinamento do modelo.
 *
 * @param totalSpamEmails  Quantidade de e-mails spam usados no treinamento.
 * @param totalHamEmails   Quantidade de e-mails ham usados no treinamento.
 * @param totalSpamWords   Total de tokens encontrados em e-mails spam.
 * @param totalHamWords    Total de tokens encontrados em e-mails ham.
 * @param vocabularySize   Número de palavras únicas no vocabulário gerado.
 * @param elapsedTimeMs    Tempo total de processamento em milissegundos.
 */
public record TrainingSummary(
        long totalSpamEmails,
        long totalHamEmails,
        long totalSpamWords,
        long totalHamWords,
        long vocabularySize,
        long elapsedTimeMs
) {}
