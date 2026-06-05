package spam.redes.bayseianas.emails.classification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LogProbabilityCalculatorTest {

    @Test
    void laplaceProbability_usesSmoothingForUnknownWord() {
        double prob = LogProbabilityCalculator.laplaceProbability(0, 1000, 500);
        assertEquals(1.0 / 1500.0, prob, 1e-12);
    }

    @Test
    void normalizeLogScores_sumsToOne() {
        double[] probs = LogProbabilityCalculator.normalizeLogScores(-1200.5, -1180.3);
        assertEquals(1.0, probs[0] + probs[1], 1e-9);
        assertTrue(probs[0] >= 0 && probs[1] >= 0);
    }

    @Test
    void normalizeLogScores_handlesVeryNegativeValuesWithoutUnderflow() {
        double[] probs = LogProbabilityCalculator.normalizeLogScores(-50_000, -50_100);
        assertEquals(1.0, probs[0] + probs[1], 1e-9);
        assertTrue(probs[0] > probs[1]);
    }

    @Test
    void normalizeLogScores_returnsFiftyFiftyWhenBothInfinite() {
        double[] probs = LogProbabilityCalculator.normalizeLogScores(
                Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
        assertEquals(0.5, probs[0], 1e-9);
        assertEquals(0.5, probs[1], 1e-9);
    }
}
