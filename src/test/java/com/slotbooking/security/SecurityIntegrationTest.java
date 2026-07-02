package com.slotbooking.security;

import com.slotbooking.BaseIntegrationTest;
import com.slotbooking.modules.user.entity.User;
import com.slotbooking.modules.user.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class SecurityIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Test
    void testRequestWithoutJwt_unauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRequestWithInvalidJwt_unauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer invalidtoken123"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testPlayerAccessingAdminEndpoint_forbidden() throws Exception {
        User player = User.builder()
                .mobileNumber("9876543201")
                .role(Role.PLAYER)
                .build();
        String token = jwtService.generateToken(player);

        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAdminAccessingAdminEndpoint_success() throws Exception {
        User admin = User.builder()
                .mobileNumber("9876543202")
                .role(Role.ADMIN)
                .build();
        String token = jwtService.generateToken(admin);

        // Accessing admin routes (e.g. users search / list) returns OK (empty list or results) instead of unauthorized/forbidden.
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
