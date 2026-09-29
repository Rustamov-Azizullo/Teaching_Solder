package uz.askar.education;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import uz.askar.education.security.Permission;
import com.jayway.jsonpath.JsonPath;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Dinamik ruxsatlar, hududlar va okrug admini doirasidagi foydalanuvchilarni boshqarish (to'liq HTTP oqimi). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DynamicPermissionsIntegrationTest {

    private static final String PASSWORD = "Parol123!";

    @Autowired
    private MockMvc mvc;

    @Test
    void adminGainsAndLosesAccessWhenRolePermissionIsToggledAtRuntime() throws Exception {
        String admin = token("okrug1");
        String superAdmin = token("superadmin");
        mvc.perform(get("/api/audit-logs").headers(auth(admin))).andExpect(status().isForbidden());
        try {
            setRolePermission(superAdmin, "ADMIN", "SYSTEM_CONFIG", true);
            // Token qayta olinmaydi: ruxsat har bir so'rovda bazadan tekshiriladi.
            mvc.perform(get("/api/audit-logs").headers(auth(admin))).andExpect(status().isOk());
        } finally {
            setRolePermission(superAdmin, "ADMIN", "SYSTEM_CONFIG", false);
        }
        mvc.perform(get("/api/audit-logs").headers(auth(admin))).andExpect(status().isForbidden());
    }

    @Test
    void userGetsExtraCapabilityThroughPersonalOverrideWithoutRoleGrant() throws Exception {
        String leader = token("katta1");
        String superAdmin = token("superadmin");
        long leaderId = currentUserId(leader);
        mvc.perform(get("/api/role-permissions").headers(auth(superAdmin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.role=='USER' && @.permission=='DASHBOARD_SURVEYS')].granted")
                        .value(hasItem(false)));
        mvc.perform(get("/api/dashboard/surveys").headers(auth(leader))).andExpect(status().isForbidden());
        try {
            mvc.perform(put("/api/users/" + leaderId + "/permissions").headers(auth(superAdmin))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[{\"permission\":\"DASHBOARD_SURVEYS\",\"granted\":true}]"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[?(@.permission=='DASHBOARD_SURVEYS')].granted").value(hasItem(true)));
            mvc.perform(get("/api/dashboard/surveys").headers(auth(leader))).andExpect(status().isOk());
            mvc.perform(get("/api/auth/me").headers(auth(leader)))
                    .andExpect(jsonPath("$.permissions", hasItem("DASHBOARD_SURVEYS")));
        } finally {
            mvc.perform(put("/api/users/" + leaderId + "/permissions").headers(auth(superAdmin))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[{\"permission\":\"DASHBOARD_SURVEYS\",\"granted\":false}]"))
                    .andExpect(status().isOk());
        }
        mvc.perform(get("/api/dashboard/surveys").headers(auth(leader))).andExpect(status().isForbidden());
        mvc.perform(get("/api/auth/me").headers(auth(leader)))
                .andExpect(jsonPath("$.permissions", not(hasItem("DASHBOARD_SURVEYS"))));
    }

    @Test
    void permissionManagementIsStaticallyRestrictedAndSuperAdminRowsBelongToMegaSuperAdmin() throws Exception {
        String superAdmin = token("superadmin");
        String mega = token("megasuperadmin");
        mvc.perform(get("/api/role-permissions").headers(auth(token("okrug1")))).andExpect(status().isForbidden());
        mvc.perform(get("/api/role-permissions").headers(auth(token("qomondon1")))).andExpect(status().isForbidden());
        mvc.perform(put("/api/role-permissions").headers(auth(superAdmin)).contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"role\":\"SUPER_ADMIN\",\"permission\":\"ADMIN\",\"granted\":false}]"))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/role-permissions").headers(auth(mega)).contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"role\":\"MEGA_SUPER_ADMIN\",\"permission\":\"ADMIN\",\"granted\":false}]"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/role-permissions").headers(auth(mega)))
                .andExpect(jsonPath("$[?(@.role=='SUPER_ADMIN')]", hasSize(Permission.values().length)))
                .andExpect(jsonPath("$[?(@.role=='MEGA_SUPER_ADMIN')]", hasSize(0)));
        mvc.perform(put("/api/role-permissions").headers(auth(mega)).contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"role\":\"SUPER_ADMIN\",\"permission\":\"DASHBOARD_OTM\",\"granted\":false}]"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/dashboard/vocational/results").headers(auth(superAdmin))).andExpect(status().isOk());
        mvc.perform(put("/api/role-permissions").headers(auth(mega)).contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"role\":\"SUPER_ADMIN\",\"permission\":\"DASHBOARD_OTM\",\"granted\":true}]"))
                .andExpect(status().isOk());
        long superAdminId = currentUserId(superAdmin);
        mvc.perform(put("/api/users/" + superAdminId + "/permissions").headers(auth(superAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"permission\":\"ADMIN\",\"granted\":true}]"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/auth/me").headers(auth(token("megasuperadmin"))))
                .andExpect(jsonPath("$.permissions", hasItem("SYSTEM_CONFIG")));
    }

    @Test
    void districtAdminSeesAndManagesOnlyUsersAndLocationsOfOwnDistrict() throws Exception {
        String admin = token("okrug1");
        String users = mvc.perform(get("/api/users").headers(auth(admin)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        List<String> usernames = JsonPath.read(users, "$[*].username");
        assertTrue(usernames.containsAll(List.of("okrug1", "adminuser", "qomondon1", "katta1")), usernames::toString);
        assertTrue(usernames.stream().noneMatch(List.of("superadmin", "megasuperadmin", "qomondon2", "katta3")::contains),
                usernames::toString);

        String locations = mvc.perform(get("/api/locations").headers(auth(admin)))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(3)))
                .andReturn().getResponse().getContentAsString();
        mvc.perform(get("/api/locations").headers(auth(token("superadmin"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$[?(@.level=='REPUBLIC')]", hasSize(1)));
        mvc.perform(get("/api/locations").headers(auth(token("katta1")))).andExpect(status().isForbidden());

        Integer ownUnitLocation = JsonPath.<List<Integer>>read(locations, "$[?(@.level=='UNIT')].id").get(0);
        long foreignUnitLocation = locationIdOfUnit(token("superadmin"), "Q-201");
        String newUser = "{\"username\":\"%s\",\"password\":\"Parol123!\",\"fullName\":\"Yangi\",\"role\":\"%s\","
                + "\"locationId\":%d}";
        mvc.perform(post("/api/users").headers(auth(admin)).contentType(MediaType.APPLICATION_JSON)
                        .content(newUser.formatted("okrug_yangi1", "USER", foreignUnitLocation)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/users").headers(auth(admin)).contentType(MediaType.APPLICATION_JSON)
                        .content(newUser.formatted("okrug_yangi2", "SUPER_ADMIN", ownUnitLocation)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/users").headers(auth(admin)).contentType(MediaType.APPLICATION_JSON)
                        .content(newUser.formatted("okrug_yangi3", "USER", ownUnitLocation)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.locationLevel").value("UNIT"))
                .andExpect(jsonPath("$.permissions", hasItem("GROUP_READ")));
    }

    private void setRolePermission(String token, String role, String permission, boolean granted) throws Exception {
        mvc.perform(put("/api/role-permissions").headers(auth(token)).contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"role\":\"" + role + "\",\"permission\":\"" + permission + "\",\"granted\":"
                                + granted + "}]"))
                .andExpect(status().isOk());
    }

    private long locationIdOfUnit(String superAdminToken, String unitCode) throws Exception {
        String body = mvc.perform(get("/api/locations").headers(auth(superAdminToken)))
                .andReturn().getResponse().getContentAsString();
        List<Integer> ids = JsonPath.read(body, "$[?(@.code=='" + unitCode + "')].id");
        return ids.get(0);
    }

    private long currentUserId(String token) throws Exception {
        String body = mvc.perform(get("/api/auth/me").headers(auth(token))).andReturn().getResponse()
                .getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
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
