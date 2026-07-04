package htw.webtech.studyhub.post;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PostControllerTest {

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

    @Test
    void getPosts_istOeffentlich() throws Exception {
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk());
    }

    @Test
    void createPost_ohneToken_gibt401() throws Exception {
        String body = """
                { "title": "Ohne Token", "type": "DOCUMENT" }
                """;
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createPost_mitToken_gibtCreated() throws Exception {
        String token = registriereUndHoleToken("poster", "poster@example.com");

        String body = """
                { "title": "Meine Zusammenfassung", "type": "DOCUMENT", "content": "Inhalt" }
                """;

        mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Meine Zusammenfassung"))
                .andExpect(jsonPath("$.userId").isNumber());
    }

    @Test
    void createPost_ohneTitel_gibt400() throws Exception {
        String token = registriereUndHoleToken("validator", "validator@example.com");

        // Kein "title" -> verletzt @NotBlank -> 400 Bad Request
        String body = """
                { "type": "DOCUMENT" }
                """;

        mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPost_mitUngueltigerUrl_gibt400() throws Exception {
        String token = registriereUndHoleToken("linkposter", "link.poster@example.com");

        // "keine-url" ist keine gültige URL -> verletzt @URL -> 400 Bad Request
        String body = """
                { "title": "Mein Link", "type": "LINK", "url": "keine-url" }
                """;

        mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPost_mitGueltigerUrl_gibtCreated() throws Exception {
        String token = registriereUndHoleToken("linkposter2", "link.poster2@example.com");

        String body = """
                { "title": "Mein Link", "type": "LINK", "url": "https://spring.io" }
                """;

        mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void deletePost_fremderUser_gibt403() throws Exception {
        // User A erstellt einen Post
        String tokenA = registriereUndHoleToken("userA", "userA@example.com");
        int postId = erstellePost(tokenA, "Post von A");

        // User B versucht, den fremden Post zu löschen -> 403 Forbidden
        String tokenB = registriereUndHoleToken("userB", "userB@example.com");
        mockMvc.perform(delete("/api/posts/" + postId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());
    }

    @Test
    void deletePost_eigenerUser_gibtNoContent() throws Exception {
        // Eigener Post darf gelöscht werden -> 204 No Content
        String token = registriereUndHoleToken("owner", "owner@example.com");
        int postId = erstellePost(token, "Eigener Post");

        mockMvc.perform(delete("/api/posts/" + postId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void getMine_ohneToken_gibt401() throws Exception {
        mockMvc.perform(get("/api/posts/mine"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMine_zeigtNurEigenePosts() throws Exception {
        // User A erstellt 2 Posts, User B einen
        String tokenA = registriereUndHoleToken("mineA", "mineA@example.com");
        erstellePost(tokenA, "Post A1");
        erstellePost(tokenA, "Post A2");
        String tokenB = registriereUndHoleToken("mineB", "mineB@example.com");
        erstellePost(tokenB, "Post B1");

        // User A darf nur seine 2 eigenen Posts sehen
        mockMvc.perform(get("/api/posts/mine")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
