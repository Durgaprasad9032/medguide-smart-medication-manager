package com.medguide.security;

import com.medguide.modules.user.domain.Role;
import com.medguide.modules.user.domain.User;
import com.medguide.modules.user.domain.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RoleBasedAccessControlTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Test
    @DisplayName("PATIENT role can access patient test route but is forbidden from doctor and admin routes")
    void shouldEnforcePatientRoleAuthorization() throws Exception {
        User patient = new User("patient_rbac@medguide.local", "hash", Role.PATIENT, UserStatus.ACTIVE, "en");
        patient.setId(101L);
        String patientToken = jwtService.generateAccessToken(patient);

        // Allowed on PATIENT endpoint
        mockMvc.perform(get("/api/v1/auth/test/patient")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.access").value("GRANTED"))
                .andExpect(jsonPath("$.data.role").value("PATIENT"));

        // Forbidden on DOCTOR endpoint
        mockMvc.perform(get("/api/v1/auth/test/doctor")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        // Forbidden on ADMIN endpoint
        mockMvc.perform(get("/api/v1/auth/test/admin")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("DOCTOR role can access doctor test route but is forbidden from admin route")
    void shouldEnforceDoctorRoleAuthorization() throws Exception {
        User doctor = new User("doctor_rbac@medguide.local", "hash", Role.DOCTOR, UserStatus.ACTIVE, "en");
        doctor.setId(102L);
        String doctorToken = jwtService.generateAccessToken(doctor);

        // Allowed on DOCTOR endpoint
        mockMvc.perform(get("/api/v1/auth/test/doctor")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.access").value("GRANTED"))
                .andExpect(jsonPath("$.data.role").value("DOCTOR"));

        // Forbidden on ADMIN endpoint
        mockMvc.perform(get("/api/v1/auth/test/admin")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("ADMIN role can access admin test route")
    void shouldEnforceAdminRoleAuthorization() throws Exception {
        User admin = new User("admin_rbac@medguide.local", "hash", Role.ADMIN, UserStatus.ACTIVE, "en");
        admin.setId(103L);
        String adminToken = jwtService.generateAccessToken(admin);

        // Allowed on ADMIN endpoint
        mockMvc.perform(get("/api/v1/auth/test/admin")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.access").value("GRANTED"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }
}
