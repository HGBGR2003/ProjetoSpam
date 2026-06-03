package spam.redes.bayseianas.emails;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import spam.redes.bayseianas.emails.Emails.ImportEmailService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StartupRunner implements CommandLineRunner {

    private final ImportEmailService service;

    @Override
    public void run(String... args) throws Exception {
        service.importSpamEmails();
        service.importHamEmails();
    }
}