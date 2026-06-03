package spam.redes.bayseianas.emails.Emails;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportEmailService {

    private final EmailRepository repository;
    private final EmailCleanerService cleanerService;

    public void importSpamEmails() throws IOException {
        importEmailsFrom("spam", true);
    }

    public void importHamEmails() throws IOException {
        importEmailsFrom("ham", false);
    }

    private void importEmailsFrom(String subpasta, boolean isSpam) throws IOException {
        Path pasta = Paths.get("dataset", subpasta);

        if (!Files.exists(pasta) || !Files.isDirectory(pasta)) {
            log.warn("Pasta dataset/{} não encontrada em: {}", subpasta, pasta.toAbsolutePath());
            return;
        }

        String tipo = isSpam ? "spam" : "ham";

        try (Stream<Path> arquivos = Files.list(pasta)) {
            long total = arquivos
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".txt"))
                    .peek(arquivo -> salvarEmail(arquivo, isSpam, subpasta))
                    .count();

            log.info("Importação concluída: {} emails {} processados.", total, tipo);
        }
    }

    private void salvarEmail(Path arquivo, boolean isSpam, String subpasta) {
        try {
            String nomeArquivo = arquivo.getFileName().toString();
            String sourceFile = subpasta + "/" + nomeArquivo;

            if (repository.existsBySourceFile(sourceFile)) {
                log.debug("Ignorado (já importado): {}", sourceFile);
                return;
            }

            String conteudoBruto = lerArquivo(arquivo);
            String conteudoLimpo = cleanerService.clean(conteudoBruto);

            if (conteudoLimpo.isBlank()) {
                log.warn("Arquivo ignorado (conteúdo vazio após limpeza): {}", sourceFile);
                return;
            }

            EmailEntity email = new EmailEntity();
            email.setSourceFile(sourceFile);
            email.setContent(conteudoLimpo);
            email.setIsSpam(isSpam);
            email.setCreatedAt(LocalDateTime.now());

            repository.save(email);
            log.debug("Salvo: {}", sourceFile);

        } catch (Exception e) {
            log.error("Erro ao processar arquivo {}: {}", arquivo.getFileName(), e.getMessage());
        }
    }

    private String lerArquivo(Path arquivo) throws IOException {
        try {
            return Files.readString(arquivo, StandardCharsets.UTF_8);
        } catch (java.nio.charset.MalformedInputException e) {
            log.debug("UTF-8 falhou para {}, tentando ISO-8859-1", arquivo.getFileName());
            return Files.readString(arquivo, Charset.forName("ISO-8859-1"));
        }
    }
}
