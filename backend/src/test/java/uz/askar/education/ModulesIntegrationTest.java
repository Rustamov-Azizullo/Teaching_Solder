package uz.askar.education;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import uz.askar.education.auth.Totp;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ModulesIntegrationTest {

    private static final String PASSWORD = "Parol123!";

    @Autowired
    private MockMvc mvc;

    @Test
    void subdivisionsFormAHierarchyAndProtectAgainstUnsafeDelete() throws Exception {
        String token = token("qomondon1");
        mvc.perform(get("/api/military-units/1/subdivisions").headers(auth(token)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].children[0].children", hasSize(2)));
        mvc.perform(get("/api/military-units/3/subdivisions").headers(auth(token))).andExpect(status().isForbidden());

        String created = mvc.perform(post("/api/military-units/1/subdivisions").headers(auth(token))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"3-vzvod\",\"parentId\":2}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        int newId = JsonPath.read(created, "$.id");
        mvc.perform(delete("/api/subdivisions/1").headers(auth(token))).andExpect(status().isConflict());
        mvc.perform(delete("/api/subdivisions/" + newId).headers(auth(token))).andExpect(status().isNoContent());
        mvc.perform(put("/api/subdivisions/1").headers(auth(token)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"1-batalon\",\"parentId\":3}"))
                .andExpect(status().isConflict());
    }

    @Test
    void soldiersCanBeFilteredByAnySubdivisionLevel() throws Exception {
        String token = token("qomondon1");
        mvc.perform(get("/api/soldiers").param("subdivisionId", "1").headers(auth(token)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(24));
        mvc.perform(get("/api/soldiers").param("subdivisionId", "3").headers(auth(token)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(12));
    }

    @Test
    void newSoldierIsRegisteredManuallyAndTransferredWithHistory() throws Exception {
        String operator = token("operator1");
        String body = "{\"pinfl\":\"31999999999999\",\"fullName\":\"Qo'lda Kiritilgan\",\"birthDate\":\"2004-05-05\","
                + "\"passport\":\"AB1234567\",\"phone\":\"+998901234567\",\"phoneKinshipId\":\"KIN\","
                + "\"regionId\":14,\"districtId\":1,\"mahalla\":\"M\",\"street\":\"S\",\"house\":\"1\","
                + "\"militaryUnitId\":1,\"subdivisionId\":3,\"generalEducation\":\"SCHOOL\",\"noPriorOccupation\":true}";
        long kinship = firstDictionaryId(operator, "KINSHIP");
        String created = mvc.perform(post("/api/soldiers").headers(auth(operator)).contentType(MediaType.APPLICATION_JSON)
                        .content(body.replace("\"KIN\"", String.valueOf(kinship))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.subdivisionPath").value("1-batalon / 1-rota / 1-vzvod"))
                .andReturn().getResponse().getContentAsString();
        int soldierId = JsonPath.read(created, "$.id");

        String commander = token("qomondon1");
        mvc.perform(post("/api/soldiers/" + soldierId + "/transfer").headers(auth(commander))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"militaryUnitId\":2,\"reason\":\"Xizmat ehtiyoji\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.toUnit").value("102-harbiy qism (demo)"));
        mvc.perform(get("/api/soldiers/" + soldierId + "/transfers").headers(auth(token("admin"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/api/soldiers/" + soldierId).headers(auth(commander))).andExpect(status().isForbidden());
    }

    @Test
    void sourceRefreshShowsDifferencesInsteadOfOverwriting() throws Exception {
        String token = token("operator1");
        mvc.perform(post("/api/soldiers/1/source-refresh").headers(auth(token)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].field").exists());
        mvc.perform(put("/api/soldiers/1/source-refresh").headers(auth(token)).contentType(MediaType.APPLICATION_JSON)
                        .content("[\"passport\"]"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/soldiers/1").headers(auth(token)))
                .andExpect(jsonPath("$.fieldSources.passport.source").value("INTEGRATION"));
    }

    @Test
    void courseResultsFlowFromEntryToApprovalAndFeedTheEmploymentList() throws Exception {
        String operator = token("operator1");
        mvc.perform(get("/api/groups/1/results").headers(auth(operator)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.rows", hasSize(12)));
        mvc.perform(put("/api/groups/1/results").headers(auth(operator)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"entries\":[{\"soldierId\":1,\"status\":\"CERTIFIED\"}]}"))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/groups/1/results/approve").headers(auth(token("qomondon1")))).andExpect(status().isOk());
        mvc.perform(put("/api/groups/1/results").headers(auth(operator)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"entries\":[{\"soldierId\":1,\"status\":\"STUDIED\"}]}"))
                .andExpect(status().isConflict());

        String hktb = token("hktb");
        mvc.perform(get("/api/employment/preview").headers(auth(hktb)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].rows", hasSize(greaterThan(0))));
        byte[] xlsx = mvc.perform(post("/api/employment/export").headers(auth(hktb)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"agency\":\"Bandlik vazirligi\",\"format\":\"XLSX\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        assertTrue(xlsx.length > 500);
        byte[] pdf = mvc.perform(post("/api/employment/export").headers(auth(hktb)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"agency\":\"Hokimlik\",\"format\":\"PDF\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        assertEquals("%PDF", new String(pdf, 0, 4));
        mvc.perform(get("/api/employment/history").headers(auth(hktb))).andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void everyReportCanBeExportedAsXlsxAndPdf() throws Exception {
        String hktb = token("hktb");
        for (String type : new String[] {"COURSE_COMPLETION", "OTM_ADMISSIONS", "YEARLY_SUMMARY"}) {
            for (String format : new String[] {"XLSX", "PDF"}) {
                mvc.perform(get("/api/reports/" + type).param("format", format).headers(auth(hktb)))
                        .andExpect(status().isOk());
            }
        }
        mvc.perform(get("/api/reports/YEARLY_SUMMARY").headers(auth(token("katta1")))).andExpect(status().isForbidden());
    }

    @Test
    void assignmentNeedsBasisDocumentToBeApproved() throws Exception {
        String created = mvc.perform(post("/api/assignments").headers(auth(token("okrug1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"militaryUnitId\":2,\"institutionId\":1,\"direction\":\"VOCATIONAL\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        int id = JsonPath.read(created, "$.id");
        String hktb = token("hktb");
        mvc.perform(post("/api/assignments/" + id + "/decision").headers(auth(hktb)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"approve\":true}"))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/assignments/" + id + "/decision").headers(auth(hktb)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"approve\":true,\"basisDocument\":\"Qo'shma qaror 5\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("APPROVED"));
        mvc.perform(get("/api/notifications/unread-count").headers(auth(hktb))).andExpect(status().isOk());
    }

    @Test
    void admissionsShowFunnelAndSyncFromBmba() throws Exception {
        String token = token("operator1");
        mvc.perform(get("/api/admissions").headers(auth(token)))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(greaterThan(5))));
        mvc.perform(post("/api/admissions/bmba-sync").headers(auth(token)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.synced").exists());
        mvc.perform(get("/api/admissions/funnel").headers(auth(token("hktb"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    void attachmentsAcceptOnlyPdfAndImagesAndAreScoped() throws Exception {
        String token = token("operator1");
        var pdf = new MockMultipartFile("file", "diplom.pdf", "application/pdf", "%PDF-1.4 demo".getBytes());
        String created = mvc.perform(multipart("/api/attachments").file(pdf).param("ownerType", "SOLDIER")
                        .param("ownerId", "1").param("kind", "Diplom").headers(auth(token)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        int id = JsonPath.read(created, "$.id");
        byte[] downloaded = mvc.perform(get("/api/attachments/" + id + "/download").headers(auth(token)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        assertEquals("%PDF-1.4 demo", new String(downloaded));
        var exe = new MockMultipartFile("file", "virus.exe", "application/octet-stream", new byte[] {1});
        mvc.perform(multipart("/api/attachments").file(exe).param("ownerType", "SOLDIER").param("ownerId", "1")
                        .headers(auth(token))).andExpect(status().isConflict());
        mvc.perform(get("/api/attachments").param("ownerType", "SOLDIER").param("ownerId", "30")
                        .headers(auth(token))).andExpect(status().isForbidden());
    }

    @Test
    void deadlinesAreSeededAndProcessingNotifiesResponsibleRoles() throws Exception {
        String admin = token("admin");
        mvc.perform(get("/api/deadlines").headers(auth(admin)))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(greaterThan(3))));
        mvc.perform(post("/api/deadlines/process").headers(auth(admin))).andExpect(status().isOk());
        mvc.perform(post("/api/deadlines/process").headers(auth(token("katta1")))).andExpect(status().isForbidden());
    }

    @Test
    void dashboardCourseResultsAreAvailable() throws Exception {
        mvc.perform(get("/api/dashboard/vocational/results").headers(auth(token("hktb"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    void twoFactorLoginRequiresValidCodeAndPasswordPolicyIsEnforced() throws Exception {
        String admin = token("admin");
        String setup = mvc.perform(post("/api/auth/2fa/setup").headers(auth(token("jtb"))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String secret = JsonPath.read(setup, "$.secret");
        String code = String.format("%06d", currentCode(secret));
        mvc.perform(post("/api/auth/2fa/enable").headers(auth(token("jtb"))).contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"" + code + "\"}")).andExpect(status().isOk());

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"jtb\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("OTP_REQUIRED"));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"jtb\",\"password\":\"" + PASSWORD + "\",\"otp\":\""
                                + String.format("%06d", currentCode(secret)) + "\"}"))
                .andExpect(status().isOk());

        mvc.perform(post("/api/users").headers(auth(admin)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"weak1\",\"password\":\"password\",\"fullName\":\"X\",\"role\":\"HKTB\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void totpMatchesRfc6238TestVector() {
        // RFC 6238 ilovasi: "12345678901234567890" (Base32: GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ), T=59 -> 94287082 (6 raqam: 287082)
        assertTrue(Totp.verify("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ", "287082", 59));
        assertFalse(Totp.verify("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ", "000000", 59));
    }

    private int currentCode(String secret) {
        return codeAt(secret, System.currentTimeMillis() / 1000);
    }

    private int codeAt(String secret, long epochSeconds) {
        try {
            var method = Totp.class.getDeclaredMethod("generate", String.class, long.class);
            method.setAccessible(true);
            return (int) method.invoke(null, secret, epochSeconds / 30);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private long firstDictionaryId(String token, String type) throws Exception {
        String body = mvc.perform(get("/api/dictionaries/" + type).headers(auth(token))).andReturn().getResponse()
                .getContentAsString();
        return ((Number) JsonPath.read(body, "$[0].id")).longValue();
    }

    private org.springframework.http.HttpHeaders auth(String token) {
        var headers = new org.springframework.http.HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    private String token(String username) throws Exception {
        ResultActions result = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"" + PASSWORD + "\"}"));
        return JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.accessToken");
    }
}
