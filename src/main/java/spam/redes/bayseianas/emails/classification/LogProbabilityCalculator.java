package spam.redes.bayseianas.emails.classification;

/**
 * Cálculos de probabilidade em log-space com suavização de Laplace.
 */
public final class LogProbabilityCalculator {

    private LogProbabilityCalculator() {}

    public static double laplaceProbability(long count, long totalWords, long vocabularySize) {
        return (count + 1.0) / (totalWords + vocabularySize);
    }

    public static double logPrior(long classEmails, long totalEmails) {
        if (classEmails <= 0 || totalEmails <= 0) {
            return Double.NEGATIVE_INFINITY;
        }
        return Math.log((double) classEmails / totalEmails);
    }

    /**
     * Converte log-scores em probabilidades normalizadas (log-sum-exp).
     *
     * @return array [pSpam, pHam] somando 1.0
     */
    public static double[] normalizeLogScores(double logScoreSpam, double logScoreHam) {
        if (Double.isInfinite(logScoreSpam) && Double.isInfinite(logScoreHam)) {
            return new double[]{0.5, 0.5};
        }

        double max = Math.max(logScoreSpam, logScoreHam);
        double expSpam = Math.exp(logScoreSpam - max);
        double expHam = Math.exp(logScoreHam - max);
        double sum = expSpam + expHam;

        if (sum == 0.0 || Double.isNaN(sum)) {
            return new double[]{0.5, 0.5};
        }

        return new double[]{expSpam / sum, expHam / sum};
    }
}
