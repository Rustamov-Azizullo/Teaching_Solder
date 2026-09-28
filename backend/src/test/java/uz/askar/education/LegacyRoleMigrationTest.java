package uz.askar.education;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

/**
 * V11 migratsiyasi mavjud (eski 15 rolli) bazadagi yozuvlarni to'g'ri ko'chirishini tekshiradi:
 * rollar vakolat darajasi bo'yicha, okrug/qism -&gt; location_id, eski qism rollari -&gt; shaxsiy ruxsatlar.
 */
class LegacyRoleMigrationTest {

    private static final String URL =
            "jdbc:h2:mem:legacy_migration;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1";

    @Test
    void v11MapsLegacyRolesLocationsAndCapabilities() throws SQLException {
        flyway("10").migrate();
        try (Connection connection = DriverManager.getConnection(URL, "sa", "");
             Statement sql = connection.createStatement()) {
            sql.execute("INSERT INTO military_districts (id, code, name) VALUES (50, 'MD-50', 'Okrug 50')");
            sql.execute("INSERT INTO military_units (id, military_district_id, code, name) VALUES (60, 50, 'U-60', 'Qism 60')");
            insertUser(sql, 1, "old_admin", "SYSTEM_ADMIN", null, null);
            insertUser(sql, 2, "old_district", "DISTRICT_OFFICER", 50L, null);
            insertUser(sql, 3, "old_commander", "UNIT_COMMANDER", null, 60L);
            insertUser(sql, 4, "old_leader", "GROUP_LEADER", null, 60L);
            insertUser(sql, 5, "prior_admin", "ADMIN", null, null);
            sql.execute("INSERT INTO deadlines (name, deadline_date, responsible_role, escalation_role, status, cycle_year) "
                    + "VALUES ('D', DATE '2026-01-01', 'UNIT_OPERATOR', 'HKTB', 'OPEN', 2026)");
        }

        flyway(null).migrate();

        try (Connection connection = DriverManager.getConnection(URL, "sa", "");
             Statement sql = connection.createStatement()) {
            assertEquals("SUPER_ADMIN", single(sql, "SELECT role FROM app_users WHERE id = 1"));
            assertNull(single(sql, "SELECT location_id FROM app_users WHERE id = 1"));
            assertEquals("ADMIN", single(sql, "SELECT role FROM app_users WHERE id = 2"));
            assertEquals("DISTRICT", single(sql,
                    "SELECT l.level FROM app_users u JOIN locations l ON l.id = u.location_id WHERE u.id = 2"));
            assertEquals("USER", single(sql, "SELECT role FROM app_users WHERE id = 3"));
            assertEquals("60", single(sql,
                    "SELECT l.military_unit_id FROM app_users u JOIN locations l ON l.id = u.location_id WHERE u.id = 3"));
            assertEquals("50", single(sql, "SELECT p.military_district_id FROM app_users u "
                    + "JOIN locations l ON l.id = u.location_id JOIN locations p ON p.id = l.parent_id WHERE u.id = 3"));
            assertEquals("ADMIN", single(sql, "SELECT role FROM app_users WHERE id = 5"));

            assertEquals(List.of("SOLDIER_READ"), list(sql, "SELECT permission FROM user_permissions WHERE user_id = 4"));
            assertEquals("22", single(sql, "SELECT COUNT(*) FROM user_permissions WHERE user_id = 3"));
            assertEquals("0", single(sql, "SELECT COUNT(*) FROM user_permissions WHERE user_id IN (1, 2, 5)"));
            assertEquals("USER", single(sql, "SELECT responsible_role FROM deadlines"));
            assertEquals("SUPER_ADMIN", single(sql, "SELECT escalation_role FROM deadlines"));
            assertEquals("1", single(sql, "SELECT COUNT(*) FROM locations WHERE level = 'REPUBLIC'"));
        }
    }

    private Flyway flyway(String target) {
        var configuration = Flyway.configure().dataSource(URL, "sa", "").cleanDisabled(false);
        if (target != null) {
            configuration.target(target);
        }
        return configuration.load();
    }

    private void insertUser(Statement sql, long id, String username, String role, Long districtId, Long unitId)
            throws SQLException {
        sql.execute("INSERT INTO app_users (id, username, password_hash, full_name, role, military_district_id, "
                + "military_unit_id) VALUES (" + id + ", '" + username + "', 'x', '" + username + "', '" + role + "', "
                + districtId + ", " + unitId + ")");
    }

    private String single(Statement sql, String query) throws SQLException {
        try (ResultSet rows = sql.executeQuery(query)) {
            rows.next();
            return rows.getString(1);
        }
    }

    private List<String> list(Statement sql, String query) throws SQLException {
        List<String> result = new ArrayList<>();
        try (ResultSet rows = sql.executeQuery(query)) {
            while (rows.next()) {
                result.add(rows.getString(1));
            }
        }
        return result;
    }
}
