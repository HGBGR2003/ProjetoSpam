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

    @Column(unique = true)
    private String sourceFile;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Boolean isSpam;

    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public String getContent() {
        return content;
    }

    public Boolean getIsSpam() {
        return isSpam;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setIsSpam(Boolean isSpam) {
        this.isSpam = isSpam;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}