package uz.askar.education;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Har bir foydalanuvchi faqat o'z vakolat doirasidagi (qism/okrug) askarlarni ko'radi va tahrirlaydi. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SoldierScopeIsolationTest {

    private static final String PASSWORD = "Parol123!";

    @Autowired
    private MockMvc mvc;

    @Test
    void unitUserSeesOnlyOwnUnitSoldiers() throws Exception {
        List<String> ownUnits = unitNames(token("qomondon1"));
        List<String> otherUnits = unitNames(token("qomondon2"));
        assertTrue(ownUnits.stream().allMatch(name -> name.startsWith("101")), ownUnits::toString);
        assertTrue(otherUnits.stream().allMatch(name -> name.startsWith("201")), otherUnits::toString);
    }

    @Test
    void soldierOfAnotherUnitCannotBeReadOrEdited() throws Exception {
        int foreignSoldierId = firstSoldierId(token("qomondon2"));
        for (String outsider : List.of("qomondon1", "katta1", "okrug1")) {
            String token = token(outsider);
            mvc.perform(get("/api/soldiers/" + foreignSoldierId).headers(auth(token))).andExpect(status().isForbidden());
            mvc.perform(put("/api/soldiers/" + foreignSoldierId).headers(auth(token))
                            .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().is4xxClientError());
        }
        mvc.perform(get("/api/soldiers/" + foreignSoldierId).headers(auth(token("superadmin"))))
                .andExpect(status().isOk());
    }

    @Test
    void sourceLookupDoesNotRevealSoldiersOutsideScope() throws Exception {
        String foreign = mvc.perform(get("/api/soldiers/" + firstSoldierId(token("qomondon2")))
                        .headers(auth(token("superadmin")))).andReturn().getResponse().getContentAsString();
        String pinfl = JsonPath.read(foreign, "$.pinfl");
        int status = mvc.perform(get("/api/soldiers/source-lookup").param("pinfl", pinfl)
                        .headers(auth(token("qomondon1"))))
                .andReturn().getResponse().getStatus();
        assertEquals(409, status);
    }

    private List<String> unitNames(String token) throws Exception {
        String body = mvc.perform(get("/api/soldiers").param("size", "100").headers(auth(token)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.content[*].militaryUnitName");
    }

    private int firstSoldierId(String token) throws Exception {
        String body = mvc.perform(get("/api/soldiers").headers(auth(token))).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.content[0].id");
    }

    private HttpHeaders auth(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    private String token(String username) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }
}
