package htw.webtech.studyhub.linkpreview;

/**
 * Ergebnis einer Link-Vorschau. Alle Felder koennen null sein
 * (z.B. wenn microlink nichts liefert oder ein Fehler auftrat).
 */
public record LinkPreviewDto(String title, String description, String imageUrl) {

    /** Leeres Ergebnis – wird bei Fehlern zurückgegeben (kein 500). */
    public static LinkPreviewDto empty() {
        return new LinkPreviewDto(null, null, null);
    }
}
