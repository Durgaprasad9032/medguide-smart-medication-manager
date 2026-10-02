package com.medguide;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DatabaseMigrationIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Flyway flyway;

    private static final Set<String> EXPECTED_TABLES = Set.of(
            "users",
            "patients",
            "doctors",
            "medicines",
            "medicine_localizations",
            "prescriptions",
            "patient_medications",
            "medication_logs",
            "doctor_patient_links",
            "refresh_tokens",
            "device_tokens",
            "admin_audit_logs",
            "flyway_schema_history"
    );

    @Test
    @DisplayName("Verify MySQL connectivity and active connection")
    void shouldConnectToDatabase() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.isValid(2)).isTrue();
            assertThat(connection.getCatalog()).isEqualToIgnoringCase("medguide_db");
        }
    }

    @Test
    @DisplayName("Verify Flyway migration executed successfully")
    void shouldExecuteFlywayMigrations() {
        MigrationInfo[] appliedMigrations = flyway.info().applied();
        assertThat(appliedMigrations).isNotEmpty();

        MigrationInfo v1 = appliedMigrations[0];
        assertThat(v1.getVersion().getVersion()).isEqualTo("1");
        assertThat(v1.getDescription()).isEqualTo("initial schema");
        assertThat(v1.getState().isApplied()).isTrue();
    }

    @Test
    @DisplayName("Verify all expected domain tables are present in database schema")
    void shouldContainAllExpectedTables() throws Exception {
        Set<String> actualTables = new HashSet<>();

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet rs = metaData.getTables("medguide_db", null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    actualTables.add(rs.getString("TABLE_NAME").toLowerCase());
                }
            }
        }

        assertThat(actualTables).containsAll(EXPECTED_TABLES);
    }
}
