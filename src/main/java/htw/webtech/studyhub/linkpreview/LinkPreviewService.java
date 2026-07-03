package htw.webtech.studyhub.linkpreview;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Service zum Abrufen von Metadaten (Titel, Beschreibung, Bild) für externe Links.
 * Dient der Vorschau in der Benutzeroberfläche.
 */
@Service
public class LinkPreviewService {

    private final RestClient restClient;

    public LinkPreviewService() {
        // Timeouts, damit ein langsamer/hängender Aufruf nicht ewig blockiert.
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(6000);
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    /**
     * Holt Titel, Beschreibung und Vorschaubild zu einer URL.
     * Nutzt für YouTube-Links die eigene oEmbed-Schnittstelle, da Drittanbieter
     * wie Microlink oft blockiert werden. Alle anderen URLs werden über microlink.io aufgelöst.
     * @param url Die aufzulösende Ziel-URL
     * @return Ein {@link LinkPreviewDto} mit den gefundenen Daten. Bei Fehlern (Netzwerk,
     * ungültige URL) wird ein leeres DTO zurückgegeben, es werden keine Exceptions geworfen.
     */
    public LinkPreviewDto fetchPreview(String url) {
        if (isYoutubeUrl(url)) {
            return fetchYoutubePreview(url);
        }
        return fetchMicrolinkPreview(url);
    }

    private boolean isYoutubeUrl(String url) {
        return url.contains("youtube.com/watch") || url.contains("youtu.be/");
    }

    private LinkPreviewDto fetchYoutubePreview(String url) {
        try {
            YoutubeOembedResponse response = restClient.get()
                    .uri("https://www.youtube.com/oembed?url={url}&format=json", url)
                    .retrieve()
                    .body(YoutubeOembedResponse.class);

            if (response == null) {
                return LinkPreviewDto.empty();
            }
            return new LinkPreviewDto(response.title(), response.authorName(), response.thumbnailUrl());
        } catch (Exception e) {
            return LinkPreviewDto.empty();
        }
    }

    private LinkPreviewDto fetchMicrolinkPreview(String url) {
        try {
            MicrolinkResponse response = restClient.get()
                    .uri("https://api.microlink.io?url={url}", url)
                    .retrieve()
                    .body(MicrolinkResponse.class);

            if (response == null || !"success".equals(response.status()) || response.data() == null) {
                return LinkPreviewDto.empty();
            }

            MicrolinkData data = response.data();
            String imageUrl = data.image() != null ? data.image().url() : null;
            return new LinkPreviewDto(data.title(), data.description(), imageUrl);
        } catch (Exception e) {
            return LinkPreviewDto.empty();
        }
    }

    // zum Einlesen der microlink-Antwort (unbekannte Felder werden ignoriert)
    private record MicrolinkResponse(String status, MicrolinkData data) {
    }

    private record MicrolinkData(String title, String description, MicrolinkImage image) {
    }

    private record MicrolinkImage(String url) {
    }

    // zum Einlesen der YouTube-oEmbed-Antwort (JSON nutzt snake_case)
    private record YoutubeOembedResponse(
            String title,
            @JsonProperty("author_name") String authorName,
            @JsonProperty("thumbnail_url") String thumbnailUrl) {
    }
}
