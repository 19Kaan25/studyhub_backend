package htw.webtech.studyhub.post;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Set;

@Service
public class PostFileService {

    // Nur Bilder und PDFs erlauben
    private static final Set<String> ERLAUBTE_TYPEN = Set.of(
            "application/pdf",
            "image/png",
            "image/jpeg");

    // Maximale Dateigröße: 5 MB
    private static final long MAX_GROESSE = 5L * 1024 * 1024;

    private final PostFileRepository fileRepo;
    private final PostRepository postRepo;

    public PostFileService(PostFileRepository fileRepo, PostRepository postRepo) {
        this.fileRepo = fileRepo;
        this.postRepo = postRepo;
    }

    /** Speichert (oder ersetzt) die Datei eines Posts. Nur der Eigentümer darf das. */
    public void store(Long postId, MultipartFile file, Long userId) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post nicht gefunden: " + postId));

        if (post.getUserId() == null || !post.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Kein Zugriff auf fremde Posts.");
        }
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Keine Datei übergeben.");
        }
        if (file.getSize() > MAX_GROESSE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datei zu groß (max. 5 MB).");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ERLAUBTE_TYPEN.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Nur PDF, PNG und JPG erlaubt.");
        }

        // Vorhandene Datei ersetzen (falls schon eine da ist), sonst neu anlegen
        PostFile postFile = fileRepo.findByPostId(postId).orElseGet(PostFile::new);
        postFile.setPostId(postId);
        postFile.setFileName(sichererDateiname(file.getOriginalFilename()));
        postFile.setContentType(contentType);
        postFile.setSize(file.getSize());
        try {
            postFile.setData(file.getBytes());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Datei konnte nicht gelesen werden.");
        }
        fileRepo.save(postFile);

        // Metadaten am Post ablegen, damit das Frontend weiß: hier gibt es einen Download
        post.setFileName(postFile.getFileName());
        postRepo.save(post);
    }

    /** Liefert die Datei eines Posts zum Herunterladen. */
    public PostFile get(Long postId) {
        return fileRepo.findByPostId(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Keine Datei für Post " + postId));
    }

    /** Löscht die Datei eines Posts (z.B. wenn der Post gelöscht wird). */
    public void deleteByPostId(Long postId) {
        fileRepo.findByPostId(postId).ifPresent(fileRepo::delete);
    }

    /** Entfernt Pfad-Anteile aus dem Dateinamen (Schutz vor Header-Injection beim Download). */
    private String sichererDateiname(String original) {
        if (original == null || original.isBlank()) {
            return "download";
        }
        // Nur den reinen Dateinamen behalten, keine Verzeichnisse
        String name = Paths.get(original).getFileName().toString();
        return name.replaceAll("[\\r\\n\"]", "_");
    }
}
