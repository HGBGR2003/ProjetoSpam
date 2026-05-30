package spam.redes.bayseianas.emails.Emails;

import java.io.IOException;
import java.nio.file.Files;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;


@Service
@RequiredArgsConstructor
public class ImportEmailService{
    private final EmailRepository repository;

    public void importSpamEmails() throws IOException {

        Path pastaSpam = Paths.get("dataset/spam");

        try (Stream<Path> arquivos = Files.list(pastaSpam)) {

            arquivos
                    .filter(Files::isRegularFile)
                    .forEach(this::salvarEmailSpam);
        }
    }

    private void salvarEmailSpam(Path arquivo) {

        try {

            String conteudo = Files.readString(arquivo);

            EmailEntity email = new EmailEntity();

            email.setContent(conteudo);
            email.setIsSpam(true);

            repository.save(email);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
