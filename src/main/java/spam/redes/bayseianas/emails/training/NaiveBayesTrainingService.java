package spam.redes.bayseianas.emails.training;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import spam.redes.bayseianas.emails.Emails.EmailEntity;
import spam.redes.bayseianas.emails.Emails.EmailRepository;
import spam.redes.bayseianas.emails.model.ModelMetadataEntity;
import spam.redes.bayseianas.emails.model.ModelMetadataRepository;
import spam.redes.bayseianas.emails.model.WordFrequencyEntity;
import spam.redes.bayseianas.emails.model.WordFrequencyRepository;

/**
 * Serviço de treinamento do classificador Naive Bayes com Suavização de Laplace.
 *
 * <p><b>Pipeline:</b>
 * <ol>
 *   <li>Lê e-mails do banco em páginas de {@value #PAGE_SIZE} registros.</li>
 *   <li>Tokeniza cada página em paralelo via {@code parallelStream()}.</li>
 *   <li>Acumula contagens em {@link ConcurrentHashMap} thread-safe, evitando race conditions.</li>
 *   <li>Persiste o vocabulário em lotes de {@value #INSERT_BATCH_SIZE} via {@code saveAll()}.</li>
 *   <li>Persiste os totais globais em {@code model_metadata}.</li>
 * </ol>
 *
 * <p><b>Fórmula gerada:</b>
 * <pre>
 *   P(w | Spam) = (spamCount(w) + 1) / (totalSpamWords + vocabularySize)   [Laplace]
 *   P(Spam)     = totalSpamEmails / (totalSpamEmails + totalHamEmails)
 * </pre>
 */
@Service
@RequiredArgsConstructor
public class NaiveBayesTrainingService {

    private static final Logger log = LoggerFactory.getLogger(NaiveBayesTrainingService.class);

    /** Registros carregados por página — equilibra latência de I/O e uso de heap. */
    private static final int PAGE_SIZE = 2_000;

    /** Registros por lote de INSERT no banco. */
    private static final int INSERT_BATCH_SIZE = 1_000;

    private final EmailRepository        emailRepository;
    private final WordFrequencyRepository wordFrequencyRepository;
    private final ModelMetadataRepository modelMetadataRepository;

    // -------------------------------------------------------------------------
    // Ponto de entrada público
    // -------------------------------------------------------------------------

    /**
     * Executa o pipeline completo de treinamento de forma idempotente
     * (apaga dados anteriores antes de iniciar).
     *
     * @return {@link TrainingSummary} com estatísticas do treinamento.
     */
    @Transactional
    public TrainingSummary trainModel() {
        long startTime = System.currentTimeMillis();
        log.info("=== Iniciando treinamento do modelo Naive Bayes ===");

        // --- 1. Limpar resultados de treinamentos anteriores ---
        wordFrequencyRepository.truncate();
        modelMetadataRepository.deleteAll();
        log.info("Dados anteriores removidos.");

        // --- 2. Estruturas de acumulação thread-safe ---
        //
        // WordCount[0] = spamCount  |  WordCount[1] = hamCount
        // ConcurrentHashMap garante visibilidade entre threads do parallelStream.
        // merge() com lambda é atômico por slot de chave no CHM.
        ConcurrentHashMap<String, long[]> wordCounts = new ConcurrentHashMap<>(100_000);

        // AtomicLong para contadores globais atualizados por múltiplas threads
        AtomicLong totalSpamEmails = new AtomicLong(0);
        AtomicLong totalHamEmails  = new AtomicLong(0);
        AtomicLong totalSpamWords  = new AtomicLong(0);
        AtomicLong totalHamWords   = new AtomicLong(0);

        // --- 3. Leitura paginada + processamento paralelo por lote ---
        long totalEmails = emailRepository.count();
        int  totalPages  = (int) Math.ceil((double) totalEmails / PAGE_SIZE);
        int  pageNumber  = 0;
        Page<EmailEntity> page;

        log.info("Total de e-mails a processar: {} | Páginas de {}: {}",
                totalEmails, PAGE_SIZE, totalPages);

        do {
            Pageable pageable = PageRequest.of(pageNumber, PAGE_SIZE);
            page = emailRepository.findAll(pageable);

            // parallelStream() distribui os e-mails desta página entre os núcleos da JVM
            page.getContent().parallelStream().forEach(email ->
                    processEmail(email, wordCounts, totalSpamEmails, totalHamEmails,
                                 totalSpamWords, totalHamWords)
            );

            logProgress(pageNumber + 1, totalPages);
            pageNumber++;

        } while (page.hasNext());

        // --- 4. Persistir vocabulário em lotes ---
        long vocabularySize = wordCounts.size();
        log.info("Vocabulário único gerado: {} palavras. Iniciando persistência em lotes...",
                vocabularySize);

        persistWordFrequencies(wordCounts);

        // --- 5. Persistir metadados globais ---
        ModelMetadataEntity metadata = ModelMetadataEntity.builder()
                .totalSpamEmails(totalSpamEmails.get())
                .totalHamEmails(totalHamEmails.get())
                .totalSpamWords(totalSpamWords.get())
                .totalHamWords(totalHamWords.get())
                .vocabularySize(vocabularySize)
                .trainedAt(LocalDateTime.now())
                .build();

        modelMetadataRepository.save(metadata);

        long elapsedMs = System.currentTimeMillis() - startTime;
        log.info("=== Treinamento concluído em {} ms ({} s) ===",
                elapsedMs, elapsedMs / 1_000);

        return new TrainingSummary(
                totalSpamEmails.get(),
                totalHamEmails.get(),
                totalSpamWords.get(),
                totalHamWords.get(),
                vocabularySize,
                elapsedMs
        );
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    /**
     * Tokeniza e contabiliza um único e-mail.
     * Executado em paralelo — não usa nenhum estado compartilhado mutável não-thread-safe.
     */
    private void processEmail(
            EmailEntity email,
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

        String content = email.getContent();
        if (content == null || content.isBlank()) {
            return;
        }

        String[] tokens = content.split("\\s+");

        for (String token : tokens) {
            if (token.isEmpty()) {
                continue;
            }

            // Descarta tokens com mais de 100 caracteres: URLs, hashes,
            // base64 e outros artefatos que não têm valor semântico para o modelo.
            if (token.length() > 100) {
                continue;
            }

            // merge é atômico por chave no ConcurrentHashMap:
            // cria [1, 0] ou [0, 1] se a chave não existe,
            // ou incrementa o slot correto se já existe.
            if (isSpam) {
                wordCounts.merge(token, new long[]{1L, 0L},
                        (existing, increment) -> { existing[0]++; return existing; });
                totalSpamWords.incrementAndGet();
            } else {
                wordCounts.merge(token, new long[]{0L, 1L},
                        (existing, increment) -> { existing[1]++; return existing; });
                totalHamWords.incrementAndGet();
            }
        }
    }

    /**
     * Converte o mapa de contagens em entidades e persiste em lotes de
     * {@value #INSERT_BATCH_SIZE} para minimizar o número de round-trips ao banco.
     */
    private void persistWordFrequencies(ConcurrentHashMap<String, long[]> wordCounts) {
        List<WordFrequencyEntity> batch = new ArrayList<>(INSERT_BATCH_SIZE);
        int batchNumber = 0;

        for (var entry : wordCounts.entrySet()) {
            long[] counts = entry.getValue();
            batch.add(WordFrequencyEntity.builder()
                    .word(entry.getKey())
                    .spamCount((int) counts[0])
                    .hamCount((int) counts[1])
                    .build());

            if (batch.size() == INSERT_BATCH_SIZE) {
                wordFrequencyRepository.saveAll(batch);
                batch.clear();
                batchNumber++;
                log.debug("Lote de INSERT #{} concluído.", batchNumber);
            }
        }

        // Persiste o lote final (tamanho < INSERT_BATCH_SIZE)
        if (!batch.isEmpty()) {
            wordFrequencyRepository.saveAll(batch);
        }
    }

    /**
     * Registra o progresso do processamento e a memória livre da JVM.
     */
    private void logProgress(int currentPage, int totalPages) {
        Runtime rt = Runtime.getRuntime();
        long freeMemoryMb = (rt.maxMemory() - rt.totalMemory() + rt.freeMemory()) / (1024 * 1024);
        log.info("Processando lote {} de {} ... Memória disponível: {} MB",
                currentPage, totalPages, freeMemoryMb);
    }
}
