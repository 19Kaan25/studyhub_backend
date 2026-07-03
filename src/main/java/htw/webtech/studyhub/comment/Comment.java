package htw.webtech.studyhub.comment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Repräsentiert einen Kommentar zu einem Post.
 * Der Autor-Name wird beim Erstellen direkt am Kommentar abgelegt, damit das Frontend
 * ihn ohne zusätzliche Abfrage anzeigen kann.
 */
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Post, zu dem der Kommentar gehört (Fremdschlüssel).
    private Long postId;

    // User, der den Kommentar geschrieben hat (Fremdschlüssel).
    private Long userId;

    // Anzeigename des Autors, beim Erstellen aus dem eingeloggten User übernommen.
    private String authorUsername;

    @NotBlank
    @Size(max = 1000)
    @Column(columnDefinition = "TEXT")
    private String content;

    private Instant createdAt;

    public Comment() {
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
