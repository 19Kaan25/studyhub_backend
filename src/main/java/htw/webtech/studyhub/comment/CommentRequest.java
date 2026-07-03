package htw.webtech.studyhub.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Eingabe-DTO für das Erstellen eines Kommentars. Nur der Inhalt kommt aus dem Request,
 * Post und Autor werden serverseitig aus Pfad und Token bestimmt.
 */
public record CommentRequest(
        @NotBlank @Size(max = 1000) String content) {
}
