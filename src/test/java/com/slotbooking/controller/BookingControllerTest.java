package com.slotbooking.controller;

import com.slotbooking.modules.booking.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Test
    @WithMockUser(roles = "PLAYER")
    void joinTournament_authenticatedPlayer_returnsOk() throws Exception {
        mockMvc.perform(post("/api/player/bookings/join/1"))
                .andExpect(status().isOk());
    }

    @Test
    void joinTournament_unauthenticated_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/player/bookings/join/1"))
                .andExpect(status().isUnauthorized());
    }
}
