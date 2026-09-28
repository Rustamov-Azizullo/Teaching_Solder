package uz.askar.education;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReferenceAndLocationManagementTest {

    private static final String PASSWORD = "Parol123!";

    @Autowired
    private MockMvc mvc;

    @Test
    void unusedDictionaryItemCanBeDeleted() throws Exception {
        String token = token("admin");
        String created = mvc.perform(post("/api/dictionaries/LANGUAGE").headers(auth(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"TMP_DEL\",\"name\":\"Vaqtinchalik\",\"active\":true}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        int id = JsonPath.read(created, "$.id");
        mvc.perform(delete("/api/dictionaries/LANGUAGE/" + id).headers(auth(token))).andExpect(status().isNoContent());
        mvc.perform(delete("/api/dictionaries/LANGUAGE/" + id).headers(auth(token))).andExpect(status().isNotFound());
    }

    @Test
    void dictionaryDeleteNeverFailsWithServerError() throws Exception {
        String token = token("admin");
        String body = mvc.perform(get("/api/dictionaries/SUBJECT").headers(auth(token))).andReturn().getResponse()
                .getContentAsString();
        int subjectId = JsonPath.read(body, "$[0].id");
        // Fan anketa/guruhlarda ishlatilgan bo'lsa 409, aks holda o'chiriladi — ikkala holat ham 5xx bo'lmasligi shart.
        int status = mvc.perform(delete("/api/dictionaries/SUBJECT/" + subjectId).headers(auth(token)))
                .andReturn().getResponse().getStatus();
        org.junit.jupiter.api.Assertions.assertTrue(status == 204 || status == 409, "status=" + status);
    }

    @Test
    void locationsCanBeCreatedRenamedAndDeletedBySuperAdminOnly() throws Exception {
        String admin = token("admin");
        String districtBody = mvc.perform(post("/api/locations").headers(auth(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test okrug\",\"code\":\"T-OK\",\"level\":\"DISTRICT\",\"parentId\":1}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.militaryDistrictId").exists())
                .andReturn().getResponse().getContentAsString();
        int districtId = JsonPath.read(districtBody, "$.id");

        String unitBody = mvc.perform(post("/api/locations").headers(auth(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test qism\",\"code\":\"T-QISM\",\"level\":\"UNIT\",\"parentId\":" + districtId + "}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.militaryUnitId").exists())
                .andReturn().getResponse().getContentAsString();
        int unitId = JsonPath.read(unitBody, "$.id");

        mvc.perform(put("/api/locations/" + unitId).headers(auth(admin)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test qism 2\",\"code\":\"T-QISM\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Test qism 2"));

        mvc.perform(delete("/api/locations/" + districtId).headers(auth(admin))).andExpect(status().isConflict());
        mvc.perform(delete("/api/locations/" + unitId).headers(auth(token("okrug1")))).andExpect(status().isForbidden());
        mvc.perform(delete("/api/locations/" + unitId).headers(auth(admin))).andExpect(status().isNoContent());
        mvc.perform(delete("/api/locations/" + districtId).headers(auth(admin))).andExpect(status().isNoContent());
        mvc.perform(delete("/api/locations/1").headers(auth(admin))).andExpect(status().isConflict());
    }

    @Test
    void geographyDashboardListsSoldiersByDistrictAndInstitutionsByRegion() throws Exception {
        mvc.perform(get("/api/dashboard/geography").headers(auth(token("hktb"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.districts", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.districts[0].units", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.regions", hasSize(greaterThan(0))));
    }

    @Test
    void otmGeographyIsGuardedByOtmDashboardPermission() throws Exception {
        mvc.perform(get("/api/dashboard/otm/geography").headers(auth(token("hktb")))).andExpect(status().isOk());
        mvc.perform(get("/api/dashboard/otm/geography").headers(auth(token("katta1")))).andExpect(status().isForbidden());
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

    @Test
    void userCanBeDeletedButNotSelf() throws Exception {
        String admin = token("admin");
        String created = mvc.perform(post("/api/users").headers(auth(admin)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"vaqtincha1\",\"password\":\"Parol123!\",\"fullName\":\"V\",\"role\":\"SUPER_ADMIN\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        int id = JsonPath.read(created, "$.id");
        mvc.perform(delete("/api/users/" + id).headers(auth(token("okrug1")))).andExpect(status().isForbidden());
        mvc.perform(delete("/api/users/" + id).headers(auth(admin))).andExpect(status().isNoContent());
        mvc.perform(delete("/api/users/" + id).headers(auth(admin))).andExpect(status().isNotFound());
        String me = mvc.perform(get("/api/auth/me").headers(auth(admin))).andReturn().getResponse().getContentAsString();
        int myId = JsonPath.read(me, "$.id");
        mvc.perform(delete("/api/users/" + myId).headers(auth(admin))).andExpect(status().isConflict());
    }
}
