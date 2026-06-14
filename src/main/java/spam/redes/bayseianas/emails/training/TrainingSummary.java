package spam.redes.bayseianas.emails.training;

public record TrainingSummary(
        long totalSpamEmails,
        long totalHamEmails,
        long totalSpamWords,
        long totalHamWords,
        long vocabularySize,
        long elapsedTimeMs
) {}
