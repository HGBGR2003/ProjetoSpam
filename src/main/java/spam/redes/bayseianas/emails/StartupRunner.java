package spam.redes.bayseianas.emails;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import spam.redes.bayseianas.emails.Emails.ImportEmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartupRunner implements CommandLineRunner {

    private final ImportEmailService service;

    @Value("${app.import.enabled:true}")
    private boolean importEnabled;

    @Override
    public void run(String... args) throws Exception {
        if (!importEnabled) {
            log.info("Importação de dataset desabilitada (app.import.enabled=false).");
            return;
        }
        service.importSpamEmails();
        service.importHamEmails();
    }
}