package htw.webtech.studyhub.comment;

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
class CommentControllerTest {

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

    /** Schreibt einen Kommentar und gibt dessen id zurück. */
    private int schreibeKommentar(String token, int postId, String text) throws Exception {
        String body = """
                { "content": "%s" }
                """.formatted(text);

        String response = mockMvc.perform(post("/api/posts/" + postId + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return JsonPath.read(response, "$.id");
    }

    @Test
    void getComments_istOeffentlich() throws Exception {
        String token = registriereUndHoleToken("maxleser", "max.leser@example.com");
        int postId = erstellePost(token, "Post mit Kommentaren");

        // Ohne Token abrufbar
        mockMvc.perform(get("/api/posts/" + postId + "/comments"))
                .andExpect(status().isOk());
    }

    @Test
    void createComment_ohneToken_gibt401() throws Exception {
        String token = registriereUndHoleToken("maxowner", "max.owner@example.com");
        int postId = erstellePost(token, "Post");

        String body = """
                { "content": "Ohne Token" }
                """;
        mockMvc.perform(post("/api/posts/" + postId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createComment_mitToken_gibtCreated() throws Exception {
        String token = registriereUndHoleToken("erika", "erika@example.com");
        int postId = erstellePost(token, "Post");

        String body = """
                { "content": "Sehr hilfreich, danke!" }
                """;

        mockMvc.perform(post("/api/posts/" + postId + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.content").value("Sehr hilfreich, danke!"))
                .andExpect(jsonPath("$.authorUsername").value("erika"));
    }

    @Test
    void createComment_ohneInhalt_gibt400() throws Exception {
        String token = registriereUndHoleToken("maxval", "max.val@example.com");
        int postId = erstellePost(token, "Post");

        // Leerer content verletzt @NotBlank -> 400 Bad Request
        String body = """
                { "content": "" }
                """;

        mockMvc.perform(post("/api/posts/" + postId + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteComment_fremderUser_gibt403() throws Exception {
        String tokenAutor = registriereUndHoleToken("autor", "autor@example.com");
        int postId = erstellePost(tokenAutor, "Post");
        int commentId = schreibeKommentar(tokenAutor, postId, "Mein Kommentar");

        // Ein anderer User darf den fremden Kommentar nicht löschen
        String tokenFremd = registriereUndHoleToken("fremder", "fremder@example.com");
        mockMvc.perform(delete("/api/posts/" + postId + "/comments/" + commentId)
                        .header("Authorization", "Bearer " + tokenFremd))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteComment_eigener_gibtNoContent() throws Exception {
        String token = registriereUndHoleToken("besitzer", "besitzer@example.com");
        int postId = erstellePost(token, "Post");
        int commentId = schreibeKommentar(token, postId, "Wird gleich gelöscht");

        mockMvc.perform(delete("/api/posts/" + postId + "/comments/" + commentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }
}
