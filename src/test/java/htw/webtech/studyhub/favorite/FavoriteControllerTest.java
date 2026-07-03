package htw.webtech.studyhub.favorite;

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
class FavoriteControllerTest {

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
    void getMine_ohneToken_gibt401() throws Exception {
        mockMvc.perform(get("/api/favorites/mine"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void favorisieren_erscheintInMeineFavoriten() throws Exception {
        String token = registriereUndHoleToken("maxfav", "max.fav@example.com");
        int postId = erstellePost(token, "Toller Post");

        // Post favorisieren
        mockMvc.perform(post("/api/posts/" + postId + "/favorite")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());

        // Taucht in den eigenen Favoriten auf
        mockMvc.perform(get("/api/favorites/mine")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(postId));
    }

    @Test
    void favorit_entfernen_leertDieListe() throws Exception {
        String token = registriereUndHoleToken("erikafav", "erika.fav@example.com");
        int postId = erstellePost(token, "Post");

        mockMvc.perform(post("/api/posts/" + postId + "/favorite")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());

        // Wieder entfernen
        mockMvc.perform(delete("/api/posts/" + postId + "/favorite")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/favorites/mine")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void favorisieren_ohneToken_gibt401() throws Exception {
        String token = registriereUndHoleToken("maxowner2", "max.owner2@example.com");
        int postId = erstellePost(token, "Post");

        mockMvc.perform(post("/api/posts/" + postId + "/favorite"))
                .andExpect(status().isUnauthorized());
    }
}
