package htw.webtech.studyhub.post;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PostFileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /** Registriert einen User und gibt dessen JWT zurück. */
    private String registriereUndHoleToken(String username, String email) throws Exception {
        String body = """
                { "username": "%s", "email": "%s", "password": "geheim123" }
                """.formatted(username, email);

        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return JsonPath.read(response, "$.token");
    }

    /** Erstellt einen Post mit dem Token und gibt dessen id zurück. */
    private int erstellePost(String token, String titel) throws Exception {
        String body = """
                { "title": "%s", "type": "DOCUMENT" }
                """.formatted(titel);

        String response = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return JsonPath.read(response, "$.id");
    }

    private MockMultipartFile pdfDatei() {
        return new MockMultipartFile("file", "skript.pdf", "application/pdf", "%PDF-1.4 Testinhalt".getBytes());
    }

    @Test
    void upload_ohneToken_gibt401() throws Exception {
        String token = registriereUndHoleToken("maxupload", "max.upload@example.com");
        int postId = erstellePost(token, "Post mit Datei");

        mockMvc.perform(multipart("/api/posts/" + postId + "/file").file(pdfDatei()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void upload_mitToken_gibtCreated() throws Exception {
        String token = registriereUndHoleToken("erikaupload", "erika.upload@example.com");
        int postId = erstellePost(token, "Post mit Datei");

        mockMvc.perform(multipart("/api/posts/" + postId + "/file")
                        .file(pdfDatei())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());
    }

    @Test
    void upload_ungueltigerDateityp_gibt400() throws Exception {
        String token = registriereUndHoleToken("maxtyp", "max.typ@example.com");
        int postId = erstellePost(token, "Post");

        MockMultipartFile textDatei =
                new MockMultipartFile("file", "notiz.txt", "text/plain", "hallo".getBytes());

        mockMvc.perform(multipart("/api/posts/" + postId + "/file")
                        .file(textDatei)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void download_nachUpload_gibtOk() throws Exception {
        String token = registriereUndHoleToken("maxdl", "max.dl@example.com");
        int postId = erstellePost(token, "Post");

        mockMvc.perform(multipart("/api/posts/" + postId + "/file")
                        .file(pdfDatei())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());

        // Download ist öffentlich (kein Token nötig)
        mockMvc.perform(get("/api/posts/" + postId + "/file"))
                .andExpect(status().isOk());
    }

    @Test
    void download_ohneVorhandeneDatei_gibt404() throws Exception {
        String token = registriereUndHoleToken("maxleer", "max.leer@example.com");
        int postId = erstellePost(token, "Post ohne Datei");

        mockMvc.perform(get("/api/posts/" + postId + "/file"))
                .andExpect(status().isNotFound());
    }
}
