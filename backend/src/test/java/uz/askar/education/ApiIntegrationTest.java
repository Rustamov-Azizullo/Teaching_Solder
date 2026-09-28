package uz.askar.education;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
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
    void swaggerAndOpenApiArePublic() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title", notNullValue()));
    }

    @Test
    void unitCommanderSeesOnlyOwnUnitSoldiers() throws Exception {
        String token = token("qomondon2");
        mvc.perform(get("/api/soldiers").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(8));
    }

    @Test
    void groupLeaderHasNoAdminOrSoldierWriteAccessButSeesOwnGroups() throws Exception {
        String token = token("katta1");
        mvc.perform(get("/api/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/soldiers/source-lookup").param("pinfl", "31234567890123")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/groups").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void dashboardIsSplitByDirectionAndRestrictedByRole() throws Exception {
        String hktb = token("hktb");
        mvc.perform(get("/api/dashboard/vocational").header("Authorization", "Bearer " + hktb))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.breakdown.level").value("DISTRICT"))
                .andExpect(jsonPath("$.daily").isNotEmpty());
        mvc.perform(get("/api/dashboard/otm").header("Authorization", "Bearer " + hktb))
                .andExpect(status().isOk());
        // Tarbiyaviy ishlar bo'limi "Kasb kurslari" blokini ko'ra olmaydi
        mvc.perform(get("/api/dashboard/vocational").header("Authorization", "Bearer " + token("tarbiya1")))
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

    @Test
    void attendanceRequiresEveryMemberAndReasonForAbsence() throws Exception {
        String token = token("katta2");
        String groupsJson = mvc.perform(get("/api/groups").header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        long groupId = Long.parseLong(groupsJson.replaceAll(".*?\"id\":(\\d+).*", "$1"));
        String lessonsJson = mvc.perform(get("/api/lessons").param("date", LocalDate.now().toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        long lessonId = Long.parseLong(lessonsJson.replaceAll(".*?\"id\":(\\d+).*", "$1"));

        mvc.perform(put("/api/lessons/" + lessonId + "/attendance").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"teacherPresent\":true,\"entries\":[{\"soldierId\":1,\"status\":\"PRESENT\"}]}"))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/lessons/" + lessonId + "/attendance").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.roster", hasSize(12)));
        org.junit.jupiter.api.Assertions.assertTrue(groupId > 0);
    }

    private String token(String username) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return body.replaceAll(".*\"accessToken\":\"([^\"]+)\".*", "$1");
    }
}
