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
        Path pastaSpam = Paths.get("dataset/spam");

        if (!Files.exists(pastaSpam) || !Files.isDirectory(pastaSpam)) {
            log.warn("Pasta dataset/spam não encontrada em: {}", pastaSpam.toAbsolutePath());
            return;
        }

        try (Stream<Path> arquivos = Files.list(pastaSpam)) {
            long total = arquivos
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".txt"))
                    .peek(this::salvarEmailSpam)
                    .count();

            log.info("Importação concluída: {} emails spam processados.", total);
        }
    }

    private void salvarEmailSpam(Path arquivo) {
        try {
            String nomeArquivo = arquivo.getFileName().toString();

            if (repository.existsBySourceFile(nomeArquivo)) {
                log.debug("Ignorado (já importado): {}", nomeArquivo);
                return;
            }

            String conteudoBruto = lerArquivo(arquivo);
            String conteudoLimpo = cleanerService.clean(conteudoBruto);

            if (conteudoLimpo.isBlank()) {
                log.warn("Arquivo ignorado (conteúdo vazio após limpeza): {}", nomeArquivo);
                return;
            }

            EmailEntity email = new EmailEntity();
            email.setSourceFile(nomeArquivo);
            email.setContent(conteudoLimpo);
            email.setIsSpam(true);
            email.setCreatedAt(LocalDateTime.now());

            repository.save(email);
            log.debug("Salvo: {}", nomeArquivo);

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
