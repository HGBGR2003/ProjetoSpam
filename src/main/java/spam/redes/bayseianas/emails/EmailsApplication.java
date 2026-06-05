package spam.redes.bayseianas.emails;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import spam.redes.bayseianas.emails.training.TrainingProperties;

@SpringBootApplication
@EnableConfigurationProperties(TrainingProperties.class)
public class EmailsApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmailsApplication.class, args);
	}

}
