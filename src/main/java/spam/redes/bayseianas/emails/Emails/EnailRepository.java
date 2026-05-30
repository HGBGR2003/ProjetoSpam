package spam.redes.bayseianas.emails.Emails;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EnailRepository  extends JpaRepository<EmailEntity, Long> {
    
}
