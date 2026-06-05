package spam.redes.bayseianas.emails.training;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import spam.redes.bayseianas.emails.Emails.EmailRepository;
import spam.redes.bayseianas.emails.classification.EmailTokenizer;
import spam.redes.bayseianas.emails.training.WordFrequencyBulkWriter.WordCountRow;

/**
 * Treinamento Naive Bayes com leitura keyset, limpeza de texto e persistência JDBC em lotes.
 */
@Service
@RequiredArgsConstructor
public class NaiveBayesTrainingService {

    private static final Logger log = LoggerFactory.getLogger(NaiveBayesTrainingService.class);

    private final EmailRepository emailRepository;
    private final TrainingMetadataService trainingMetadataService;
    private final TrainingDataResetService trainingDataResetService;
    private final WordFrequencyBulkWriter wordFrequencyBulkWriter;
    private final EmailTokenizer emailTokenizer;
    private final TrainingProperties trainingProperties;

    public TrainingSummary trainModel() {
        return trainModel(null, null);
    }

    public TrainingSummary trainModel(UUID jobId, TrainingProgressCallback progressCallback) {
        long startTime = System.currentTimeMillis();
        String jobLabel = jobId != null ? "[" + jobId + "] " : "";
        log.info("{}=== Iniciando treinamento do modelo Naive Bayes ===", jobLabel);

        trainingDataResetService.resetPreviousTraining();
        log.info("{}Dados anteriores removidos.", jobLabel);

        int batchSize = trainingProperties.getBatchSize();
        int insertBatchSize = trainingProperties.getInsertBatchSize();
        long maxEmails = trainingProperties.getMaxEmails();

        ConcurrentHashMap<String, long[]> wordCounts = new ConcurrentHashMap<>(100_000);
        AtomicLong totalSpamEmails = new AtomicLong(0);
        AtomicLong totalHamEmails = new AtomicLong(0);
        AtomicLong totalSpamWords = new AtomicLong(0);
        AtomicLong totalHamWords = new AtomicLong(0);

        long totalInDb = emailRepository.count();
        long totalToProcess = maxEmails > 0 ? Math.min(maxEmails, totalInDb) : totalInDb;
        int totalBatches = totalToProcess == 0
                ? 0
                : (int) Math.ceil((double) totalToProcess / batchSize);

        log.info("{}E-mails a processar: {} (banco: {}, batch-size: {}, max-emails: {})",
                jobLabel, totalToProcess, totalInDb, batchSize, maxEmails);

        long lastId = 0;
        long processed = 0;
        int batchNumber = 0;

        while (processed < totalToProcess) {
            int pageSize = (int) Math.min(batchSize, totalToProcess - processed);
            List<EmailTrainingProjection> batch = emailRepository.findTrainingBatch(
                    lastId, PageRequest.of(0, pageSize));

            if (batch.isEmpty()) {
                break;
            }

            batch.parallelStream().forEach(email ->
                    processEmail(email, wordCounts, totalSpamEmails, totalHamEmails,
                            totalSpamWords, totalHamWords));

            lastId = batch.get(batch.size() - 1).getId();
            processed += batch.size();
            batchNumber++;

            int progressPercent = totalBatches == 0
                    ? 100
                    : (int) Math.min(99, (batchNumber * 100L) / totalBatches);

            if (progressCallback != null) {
                progressCallback.onProgress(batchNumber, totalBatches, progressPercent);
            }

            logProgress(jobLabel, batchNumber, totalBatches, progressPercent);
        }

        long vocabularySize = wordCounts.size();
        log.info("{}Vocabulário único: {} palavras. Persistindo via JDBC...", jobLabel, vocabularySize);

        persistWordFrequencies(wordCounts, insertBatchSize);

        if (progressCallback != null && totalBatches > 0) {
            progressCallback.onProgress(totalBatches, totalBatches, 99);
        }

        trainingMetadataService.save(
                totalSpamEmails.get(),
                totalHamEmails.get(),
                totalSpamWords.get(),
                totalHamWords.get(),
                vocabularySize);

        long elapsedMs = System.currentTimeMillis() - startTime;
        TrainingSummary summary = new TrainingSummary(
                totalSpamEmails.get(),
                totalHamEmails.get(),
                totalSpamWords.get(),
                totalHamWords.get(),
                vocabularySize,
                elapsedMs);

        if (progressCallback != null) {
            progressCallback.onProgress(totalBatches, totalBatches, 100);
        }

        log.info("{}=== Treinamento concluído em {} ms ({} s) ===",
                jobLabel, summary.elapsedTimeMs(), summary.elapsedTimeMs() / 1_000);

        return summary;
    }

    private void processEmail(
            EmailTrainingProjection email,
            ConcurrentHashMap<String, long[]> wordCounts,
            AtomicLong totalSpamEmails,
            AtomicLong totalHamEmails,
            AtomicLong totalSpamWords,
            AtomicLong totalHamWords) {

        boolean isSpam = Boolean.TRUE.equals(email.getIsSpam());

        if (isSpam) {
            totalSpamEmails.incrementAndGet();
        } else {
            totalHamEmails.incrementAndGet();
        }

        List<String> tokens = emailTokenizer.tokenizeFromRaw(email.getContent());
        if (tokens.isEmpty()) {
            return;
        }

        for (String token : tokens) {
            if (isSpam) {
                wordCounts.merge(token, new long[]{1L, 0L},
                        (existing, increment) -> {
                            existing[0]++;
                            return existing;
                        });
                totalSpamWords.incrementAndGet();
            } else {
                wordCounts.merge(token, new long[]{0L, 1L},
                        (existing, increment) -> {
                            existing[1]++;
                            return existing;
                        });
                totalHamWords.incrementAndGet();
            }
        }
    }

    private void persistWordFrequencies(
            ConcurrentHashMap<String, long[]> wordCounts,
            int insertBatchSize) {

        List<WordCountRow> batch = new ArrayList<>(insertBatchSize);
        int batchNumber = 0;

        for (var entry : wordCounts.entrySet()) {
            long[] counts = entry.getValue();
            batch.add(new WordCountRow(
                    entry.getKey(),
                    toIntCount(counts[0]),
                    toIntCount(counts[1])));

            if (batch.size() == insertBatchSize) {
                wordFrequencyBulkWriter.insertBatch(batch);
                batch.clear();
                batchNumber++;
                log.debug("Lote JDBC #{} concluído.", batchNumber);
            }
        }

        if (!batch.isEmpty()) {
            wordFrequencyBulkWriter.insertBatch(batch);
        }
    }

    private static int toIntCount(long value) {
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }

    private void logProgress(String jobLabel, int currentBatch, int totalBatches, int progressPercent) {
        Runtime rt = Runtime.getRuntime();
        long freeMemoryMb = (rt.maxMemory() - rt.totalMemory() + rt.freeMemory()) / (1024 * 1024);
        log.info("{}Treinamento: lote {} de {} ({}%) — memória disponível: {} MB",
                jobLabel, currentBatch, totalBatches, progressPercent, freeMemoryMb);
    }
}
