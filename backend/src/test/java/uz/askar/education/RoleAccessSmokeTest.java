package uz.askar.education;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Har bir demo rol uchun asosiy GET endpointlar 5xx xatosi bermasligi (faqat 200 yoki 403) tekshiriladi. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RoleAccessSmokeTest {

    private static final String PASSWORD = "Parol123!";
    private static final List<String> DEMO_USERS = List.of(
            "admin", "hktb", "jtb", "tmibb", "okrug1", "qomondon1", "operator1", "jangovar1", "tarbiya1",
            "katta1", "psixolog1");

    @Autowired
    private MockMvc mvc;

    @Test
    void readEndpointsNeverFailWithServerErrorForAnyRole() throws Exception {
        String today = LocalDate.now().toString();
        List<String> endpoints = List.of(
                "/api/auth/me", "/api/regions", "/api/regions/1/districts", "/api/military-districts",
                "/api/military-units", "/api/dictionaries", "/api/dictionaries/SUBJECT",
                "/api/dictionaries/PROFESSION_DIRECTION?unitId=1", "/api/military-units/1/directions",
                "/api/soldiers", "/api/soldiers/1", "/api/soldiers/1/questionnaire", "/api/groups", "/api/groups/1",
                "/api/teachers", "/api/institutions", "/api/lessons?date=" + today,
                "/api/groups/1/lessons?from=" + today + "&to=" + today,
                "/api/dashboard/surveys", "/api/settings",
                "/api/users", "/api/users/roles", "/api/audit-logs", "/api/military-units/1/group-leaders",
                "/api/military-units/1/subdivisions", "/api/soldiers/1/transfers",
                "/api/assignments", "/api/groups/1/results", "/api/admissions",
                "/api/admissions/funnel", "/api/admissions/reserve-list", "/api/employment/preview",
                "/api/employment/history", "/api/deadlines", "/api/notifications", "/api/notifications/unread-count",
                "/api/cycles", "/api/integration-logs", "/api/surveys/group-suggestions",
                "/api/dashboard/vocational/results", "/api/reports/YEARLY_SUMMARY",
                "/api/attachments?ownerType=SOLDIER&ownerId=1");

        for (String username : DEMO_USERS) {
            String token = token(username);
            for (String endpoint : endpoints) {
                int status = mvc.perform(get(endpoint).header("Authorization", "Bearer " + token))
                        .andReturn().getResponse().getStatus();
                assertTrue(status < 500, () -> username + " GET " + endpoint + " -> " + status);
            }
        }
    }

    private String token(String username) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andReturn().getResponse().getContentAsString();
        return body.replaceAll(".*\"accessToken\":\"([^\"]+)\".*", "$1");
    }
}
