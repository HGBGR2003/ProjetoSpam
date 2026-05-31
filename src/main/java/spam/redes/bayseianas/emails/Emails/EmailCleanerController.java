package spam.redes.bayseianas.emails.Emails;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/email-cleaner")
public class EmailCleanerController {

    private final EmailCleanerService cleanerService;

    public EmailCleanerController(EmailCleanerService cleanerService) {
        this.cleanerService = cleanerService;
    }

    @PostMapping(value = "/text", consumes = "text/plain", produces = "text/plain")
    public ResponseEntity<String> cleanText(@RequestBody String rawEmail) {
        String cleaned = cleanerService.clean(rawEmail);
        return ResponseEntity.ok(cleaned);
    }

    @PostMapping(value = "/file", produces = "text/plain")
    public ResponseEntity<String> cleanFile(@RequestParam("file") MultipartFile file)
            throws IOException {
        String raw = new String(file.getBytes(), StandardCharsets.UTF_8);
        String cleaned = cleanerService.clean(raw);
        return ResponseEntity.ok(cleaned);
    }

    @PostMapping(value = "/batch", produces = "application/json")
    public ResponseEntity<Map<String, String>> cleanBatch(
            @RequestParam("files") List<MultipartFile> files) {

        Map<String, String> results = new LinkedHashMap<>();

        for (MultipartFile file : files) {
            try {
                String raw = new String(file.getBytes(), StandardCharsets.UTF_8);
                String cleaned = cleanerService.clean(raw);
                results.put(file.getOriginalFilename(), cleaned);
            } catch (IOException e) {
                results.put(file.getOriginalFilename(),
                        "ERRO ao ler arquivo: " + e.getMessage());
            }
        }

        return ResponseEntity.ok(results);
    }
}