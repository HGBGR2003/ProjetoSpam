package spam.redes.bayseianas.emails.Emails;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;     
import jakarta.persistence.Table;

@Entity
@Table(name = "emails")
public class EmailEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Boolean isSpam;

    private LocalDateTime createdAt;
}
