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
    void geographyDashboardListsSoldiersByDistrictAndUnit() throws Exception {
        mvc.perform(get("/api/dashboard/geography").headers(auth(token("hktb"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.districts", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.districts[0].units", hasSize(greaterThan(0))));
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

    @Test
    void unitUserCanContractInstitutionsOnlyToOwnUnit() throws Exception {
        String user = token("user");
        int ownUnitId = firstId(user, "/api/military-units", "$[0].id");
        String allUnits = mvc.perform(get("/api/military-units").headers(auth(token("superadmin"))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        int otherUnitId = ((java.util.List<Integer>) JsonPath.read(allUnits, "$[*].id")).stream()
                .filter(id -> id != ownUnitId).findFirst().orElseThrow();
        String institution = mvc.perform(post("/api/institutions").headers(auth(user)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"SCHOOL\",\"name\":\"Qism chegarasi maktabi\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        int institutionId = JsonPath.read(institution, "$.id");

        mvc.perform(post("/api/institution-contracts").headers(auth(user)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"institutionId\":" + institutionId + ",\"unitId\":" + otherUnitId + "}"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/locations/tree").headers(auth(user))).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.level=='UNIT')]", hasSize(1)));
    }

    @Test
    void unitUserManagesTeachersGroupsAndLeaders() throws Exception {
        String user = token("user");
        int unitId = firstId(user, "/api/military-units", "$[0].id");
        int specialtyId = firstId(user, "/api/dictionaries/SUBJECT", "$[0].id");
        String institution = mvc.perform(post("/api/institutions").headers(auth(user)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"TECHNICAL_SCHOOL\",\"name\":\"Shartnomali texnikum\",\"subjectIds\":[" + specialtyId + "]}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.subjectIds[0]").value(specialtyId))
                .andReturn().getResponse().getContentAsString();
        int institutionId = JsonPath.read(institution, "$.id");
        mvc.perform(post("/api/institution-contracts").headers(auth(user)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"institutionId\":" + institutionId + ",\"unitId\":" + unitId + "}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/institution-contracts").headers(auth(user)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"institutionId\":" + institutionId + ",\"unitId\":" + unitId + "}"))
                .andExpect(status().isConflict());
        String second = mvc.perform(post("/api/institutions").headers(auth(user)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"SCHOOL\",\"name\":\"Almashtiriladigan maktab\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        int secondId = JsonPath.read(second, "$.id");
        mvc.perform(put("/api/institution-contracts?institutionId=" + institutionId + "&unitId=" + unitId).headers(auth(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"institutionId\":" + secondId + ",\"unitId\":" + unitId + "}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.institutionId").value(secondId));
        mvc.perform(put("/api/institution-contracts?institutionId=" + secondId + "&unitId=" + unitId).headers(auth(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"institutionId\":" + institutionId + ",\"unitId\":" + unitId + "}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.institutionId").value(institutionId));
        int professionId = firstId(user, "/api/dictionaries/PROFESSION", "$[0].id");

        String teacher = mvc.perform(post("/api/teachers").headers(auth(user)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Sinov O'qituvchi\",\"specialtyIds\":[" + specialtyId + "],\"institutionId\":"
                                + institutionId + "}"))
                .andExpect(status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();
        int teacherId = JsonPath.read(teacher, "$.id");
        mvc.perform(get("/api/institutions?unitId=" + unitId).headers(auth(user)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[?(@.id==" + institutionId + ")]").isNotEmpty());
        String uncontracted = mvc.perform(post("/api/institutions").headers(auth(user)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"SCHOOL\",\"name\":\"Shartnomasiz maktab\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        int uncontractedId = JsonPath.read(uncontracted, "$.id");
        String outsider = mvc.perform(post("/api/teachers").headers(auth(user)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Shartnomasiz\",\"specialtyIds\":[" + specialtyId + "],\"institutionId\":"
                                + uncontractedId + "}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        int outsiderId = JsonPath.read(outsider, "$.id");
        String groupBody = "{\"name\":\"Sinov guruhi\",\"type\":\"VOCATIONAL\",\"militaryUnitId\":" + unitId
                + ",\"professionId\":" + professionId + ",\"startDate\":\"" + java.time.LocalDate.now()
                + "\",\"endDate\":\"" + java.time.LocalDate.now().plusMonths(3) + "\",\"leader\":"
                + "{\"fullName\":\"Sinov Katta\",\"pinfl\":\"12345678901234\",\"militaryRank\":\"Serjant\"}}";
        String group = mvc.perform(post("/api/groups").headers(auth(user)).contentType(MediaType.APPLICATION_JSON)
                        .content(groupBody))
                .andExpect(status().is2xxSuccessful()).andExpect(jsonPath("$.leader.pinfl").value("12345678901234"))
                .andReturn().getResponse().getContentAsString();
        int groupId = JsonPath.read(group, "$.id");

        mvc.perform(put("/api/groups/" + groupId).headers(auth(user)).contentType(MediaType.APPLICATION_JSON)
                        .content(groupBody.replace("Sinov Katta", "Sinov Katta 2")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.leader.fullName").value("Sinov Katta 2"));
        mvc.perform(post("/api/groups").headers(auth(user)).contentType(MediaType.APPLICATION_JSON)
                        .content(groupBody.replace("12345678901234", "123")))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/groups/" + groupId + "/teachers").headers(auth(user))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"teacherIds\":[" + teacherId + "]}"))
                .andExpect(status().isOk());
        mvc.perform(put("/api/groups/" + groupId + "/teachers").headers(auth(user))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"teacherIds\":[" + outsiderId + "]}"))
                .andExpect(status().isConflict());
        mvc.perform(delete("/api/groups/" + groupId + "/leader").headers(auth(user)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.leader").doesNotExist());
        mvc.perform(delete("/api/teachers/" + teacherId).headers(auth(user))).andExpect(status().isNoContent());
        mvc.perform(delete("/api/groups/" + groupId).headers(auth(user))).andExpect(status().isNoContent());
        mvc.perform(delete("/api/groups/" + groupId).headers(auth(user))).andExpect(status().isNotFound());
        mvc.perform(delete("/api/teachers/" + teacherId).headers(auth(token("qomondon2")))).andExpect(status().isNotFound());
    }

    private int firstId(String token, String url, String path) throws Exception {
        String body = mvc.perform(get(url).headers(auth(token))).andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString();
        return JsonPath.read(body, path);
    }

    @Test
    void institutionCanBeRenamedAndDeletedUnlessInUse() throws Exception {
        String user = token("user");
        String created = mvc.perform(post("/api/institutions").headers(auth(user)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"SCHOOL\",\"name\":\"Sinov maktabi\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        int id = JsonPath.read(created, "$.id");
        mvc.perform(put("/api/institutions/" + id).headers(auth(user)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"TRAINING_CENTER\",\"name\":\"Sinov markazi\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Sinov markazi"));
        mvc.perform(delete("/api/institutions/" + id).headers(auth(user))).andExpect(status().isNoContent());
        mvc.perform(delete("/api/institutions/" + id).headers(auth(user))).andExpect(status().isNotFound());
        int usedId = firstId(user, "/api/institutions", "$[0].id");
        int status = mvc.perform(delete("/api/institutions/" + usedId).headers(auth(user))).andReturn().getResponse()
                .getStatus();
        org.junit.jupiter.api.Assertions.assertTrue(status == 204 || status == 409, "status=" + status);
    }
}
