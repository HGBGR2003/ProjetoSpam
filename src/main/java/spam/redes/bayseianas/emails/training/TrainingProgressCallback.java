package spam.redes.bayseianas.emails.training;

@FunctionalInterface
public interface TrainingProgressCallback {

    void onProgress(int currentBatch, int totalBatches, int progressPercent);
}
