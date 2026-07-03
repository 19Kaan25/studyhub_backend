package htw.webtech.studyhub.linkpreview;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-Controller für die Link-Vorschau.
 * Stellt einen API-Endpunkt bereit, über den Client-Anwendungen (z. B. das Frontend)
 * Metadaten zu externen URLs abrufen können.
 */
@RestController
@RequestMapping("/api/link-preview")
public class LinkPreviewController {

    private final LinkPreviewService service;

    public LinkPreviewController(LinkPreviewService service) {
        this.service = service;
    }

    /**
     * Ruft die Metadaten (Titel, Beschreibung, Vorschaubild) für eine bestimmte URL ab.
     * @param url Die Ziel-URL, für die die Vorschau generiert werden soll (als Query-Parameter).
     * @return Ein {@link LinkPreviewDto} mit den abgerufenen Daten. Schlägt der Abruf fehl
     * oder ist die URL ungültig, wird ein leeres DTO zurückgegeben.
     */
    @GetMapping
    public LinkPreviewDto getPreview(@RequestParam String url) {
        return service.fetchPreview(url);
    }
}
