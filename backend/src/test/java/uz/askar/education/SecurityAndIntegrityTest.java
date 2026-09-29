package uz.askar.education;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Token bekor qilinishi, kirish xabarlari, guruh tarkibi qoidalari va fayl imzosi tekshiruvlari. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAndIntegrityTest {

    private static final String PASSWORD = "Parol123!";

    @Autowired
    private MockMvc mvc;

    @Test
    void tokenStopsWorkingOnceItsUserIsRemoved() throws Exception {
        String mega = token("megasuperadmin");
        String locations = mvc.perform(get("/api/locations").headers(auth(mega))).andReturn().getResponse()
                .getContentAsString();
        int unitLocation = JsonPath.<List<Integer>>read(locations, "$[?(@.level=='UNIT')].id").get(0);
        String created = mvc.perform(post("/api/users").headers(auth(mega)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"vaqtincha1\",\"password\":\"Parol123!\",\"fullName\":\"Vaqtincha\","
                                + "\"role\":\"USER\",\"locationId\":" + unitLocation + "}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String temporaryToken = token("vaqtincha1");
        mvc.perform(get("/api/auth/me").headers(auth(temporaryToken))).andExpect(status().isOk());

        int userId = JsonPath.read(created, "$.id");
        mvc.perform(delete("/api/users/" + userId).headers(auth(mega))).andExpect(status().isNoContent());

        mvc.perform(get("/api/auth/me").headers(auth(temporaryToken))).andExpect(status().isUnauthorized());
    }

    @Test
    void unknownUserAndWrongPasswordGetIdenticalResponses() throws Exception {
        String unknown = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"mavjud_emas\",\"password\":\"noto'g'ri-parol\"}"))
                .andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
        String wrongPassword = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"hktb\",\"password\":\"noto'g'ri-parol\"}"))
                .andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
        assertEquals((String) JsonPath.read(unknown, "$.message"), JsonPath.read(wrongPassword, "$.message"));
    }

    @Test
    void soldierCannotBelongToTwoGroupsAndRemovedMembersLoseTheirResults() throws Exception {
        String commander = token("qomondon1");
        int soldierId = moveOneSoldierOutOfOtmGroup(commander);
        int groupId = createVocationalGroup(commander);

        mvc.perform(put("/api/groups/" + groupId + "/members").headers(auth(commander))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"soldierIds\":[" + soldierId + "]}"))
                .andExpect(status().isOk());
        mvc.perform(put("/api/groups/2/members").headers(auth(commander)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"soldierIds\":[" + soldierId + "]}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.message", containsString("boshqa guruhda")));

        mvc.perform(put("/api/groups/" + groupId + "/results").headers(auth(commander))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"entries\":[{\"soldierId\":" + soldierId + ",\"status\":\"STUDIED\"}]}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.rows[0].status").value("STUDIED"));
        mvc.perform(put("/api/groups/" + groupId + "/members").headers(auth(commander))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"soldierIds\":[]}"))
                .andExpect(status().isOk());
        mvc.perform(put("/api/groups/" + groupId + "/members").headers(auth(commander))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"soldierIds\":[" + soldierId + "]}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/groups/" + groupId + "/results").headers(auth(commander))).andExpect(status().isOk())
                .andExpect(jsonPath("$.rows[0].status").doesNotExist());
    }

    @Test
    void groupWithMembersCannotSwitchUnitOrType() throws Exception {
        String commander = token("qomondon1");
        int soldierId = moveOneSoldierOutOfOtmGroup(commander);
        int groupId = createVocationalGroup(commander);
        mvc.perform(put("/api/groups/" + groupId + "/members").headers(auth(commander))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"soldierIds\":[" + soldierId + "]}"))
                .andExpect(status().isOk());

        mvc.perform(put("/api/groups/" + groupId).headers(auth(commander)).contentType(MediaType.APPLICATION_JSON)
                        .content(groupBody("OTM_PREP")))
                .andExpect(status().isConflict());
        mvc.perform(put("/api/groups/" + groupId).headers(auth(commander)).contentType(MediaType.APPLICATION_JSON)
                        .content(groupBody("VOCATIONAL")))
                .andExpect(status().isOk());
    }

    @Test
    void uploadedFileMustMatchItsDeclaredType() throws Exception {
        String commander = token("qomondon1");
        String soldiers = mvc.perform(get("/api/soldiers").headers(auth(commander))).andReturn().getResponse()
                .getContentAsString();
        int soldierId = JsonPath.read(soldiers, "$.content[0].id");

        mvc.perform(multipart("/api/attachments").file(new MockMultipartFile("file", "fake.pdf", "application/pdf",
                                "bu PDF emas".getBytes())).param("ownerType", "SOLDIER").param("ownerId", "" + soldierId)
                        .headers(auth(commander)))
                .andExpect(status().isConflict());
        mvc.perform(multipart("/api/attachments").file(new MockMultipartFile("file", "real.pdf", "application/pdf",
                                "%PDF-1.4 sinov".getBytes())).param("ownerType", "SOLDIER")
                        .param("ownerId", "" + soldierId).headers(auth(commander)))
                .andExpect(status().isCreated());
    }

    private static final String NEW_GROUP_NAME = "Sinov guruhi";

    /** OTM tayyorlov guruhidan bitta askarni chiqaradi va uning id sini qaytaradi (u endi hech qaysi guruhda emas). */
    private int moveOneSoldierOutOfOtmGroup(String token) throws Exception {
        String otm = mvc.perform(get("/api/groups/2").headers(auth(token))).andReturn().getResponse()
                .getContentAsString();
        List<Integer> memberIds = JsonPath.read(otm, "$.members[*].id");
        int movedId = memberIds.get(0);
        mvc.perform(put("/api/groups/2/members").headers(auth(token)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"soldierIds\":" + memberIds.subList(1, memberIds.size()) + "}"))
                .andExpect(status().isOk());
        return movedId;
    }

    private int createVocationalGroup(String token) throws Exception {
        String created = mvc.perform(post("/api/groups").headers(auth(token)).contentType(MediaType.APPLICATION_JSON)
                        .content(groupBody("VOCATIONAL")))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(created, "$.id");
    }

    private String groupBody(String type) {
        java.time.LocalDate start = java.time.LocalDate.now();
        return "{\"name\":\"" + NEW_GROUP_NAME + "\",\"type\":\"" + type + "\",\"militaryUnitId\":1,"
                + "\"professionId\":33,\"subjectIds\":[1],\"startDate\":\"" + start + "\",\"endDate\":\""
                + start.plusMonths(3) + "\"}";
    }

    private HttpHeaders auth(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    private String token(String username) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }
}
