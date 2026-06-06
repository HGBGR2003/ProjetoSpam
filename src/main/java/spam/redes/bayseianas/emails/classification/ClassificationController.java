package spam.redes.bayseianas.emails.classification;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ClassificationController {

    private final NaiveBayesClassifierService classifierService;

    @PostMapping("/classify")
    public ResponseEntity<ClassificationResponse> classify(
            @RequestBody ClassificationRequest request) {
        ClassificationResponse response = classifierService.classify(request.text());
        return ResponseEntity.ok(response);
    }
}
