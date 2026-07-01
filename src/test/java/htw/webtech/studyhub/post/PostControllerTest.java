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
}
