package com.slotbooking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slotbooking.modules.notification.dto.BroadcastRequest;
import com.slotbooking.modules.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AdminNotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void broadcastAnnouncement_admin_returnsOk() throws Exception {
        BroadcastRequest request = BroadcastRequest.builder()
                .title("Maintenance Break")
                .message("The app will go offline tonight.")
                .build();

        mockMvc.perform(post("/api/admin/notifications/broadcast")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "PLAYER")
    void broadcastAnnouncement_player_forbidden() throws Exception {
        BroadcastRequest request = BroadcastRequest.builder()
                .title("Illegal Broadcast")
                .message("Players cannot do this.")
                .build();

        mockMvc.perform(post("/api/admin/notifications/broadcast")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
