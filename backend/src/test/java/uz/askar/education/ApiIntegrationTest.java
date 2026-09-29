package uz.askar.education;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {

    private static final String PASSWORD = "Parol123!";

    @Autowired
    private MockMvc mvc;

    @Test
    void loginRejectsWrongPasswordAndAcceptsDemoUser() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()));
    }

    @Test
    void protectedEndpointsRequireToken() throws Exception {
        mvc.perform(get("/api/soldiers")).andExpect(status().isUnauthorized());
    }

    @Test
    void apiDocsAreDisabledByDefault() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isNotFound());
    }

    @Test
    void unknownEndpointAndBadParametersAreClientErrors() throws Exception {
        String token = token("hktb");
        mvc.perform(get("/api/no-such-endpoint").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/soldiers").param("size", "0").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(get("/api/groups").param("type", "NOPE").header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unitUserSeesOnlyOwnUnitSoldiers() throws Exception {
        String token = token("qomondon2");
        mvc.perform(get("/api/soldiers").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(8));
    }

    @Test
    void unitUserWithoutAdminPermissionHasNoAdminOrSoldierWriteAccess() throws Exception {
        String token = token("katta1");
        mvc.perform(get("/api/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/soldiers/source-lookup").param("pinfl", "31234567890123")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void dashboardIsSplitByDirectionAndRestrictedByPermission() throws Exception {
        String hktb = token("hktb");
        mvc.perform(get("/api/dashboard/vocational/results").header("Authorization", "Bearer " + hktb))
                .andExpect(status().isOk());
        mvc.perform(get("/api/admissions/funnel").header("Authorization", "Bearer " + hktb))
                .andExpect(status().isOk());
        // Jangovar tayyorgarlik bo'limi xodimi (USER + kasb dashboardi ruxsati) OTM qabul voronkasini ko'ra olmaydi
        mvc.perform(get("/api/admissions/funnel").header("Authorization", "Bearer " + token("jangovar1")))
                .andExpect(status().isForbidden());
    }

    @Test
    void sourceLookupPrefillsAndReportsUnavailable() throws Exception {
        String token = token("operator1");
        mvc.perform(get("/api/soldiers/source-lookup").param("pinfl", "31234567890123")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.found").value(true));
        mvc.perform(get("/api/soldiers/source-lookup").param("pinfl", "91234567890123")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isServiceUnavailable());
    }

    private String token(String username) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return body.replaceAll(".*\"accessToken\":\"([^\"]+)\".*", "$1");
    }
}
