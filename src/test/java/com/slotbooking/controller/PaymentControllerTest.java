package com.slotbooking.controller;

import com.slotbooking.modules.payment.service.PaymentService;
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
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Test
    @WithMockUser(roles = "PLAYER")
    void createOrder_authenticatedPlayer_returnsOk() throws Exception {
        mockMvc.perform(post("/api/player/payments/create-order")
                .contentType("application/json")
                .content("{\"bookingId\":100}"))
                .andExpect(status().isOk());
    }

    @Test
    void processWebhook_returnsOk() throws Exception {
        String signature = "mock_razorpay_signature";
        String payload = "{ \"event\": \"payment.captured\" }";

        mockMvc.perform(post("/api/webhooks/razorpay")
                .header("x-razorpay-signature", signature)
                .contentType("application/json")
                .content(payload))
                .andExpect(status().isOk());
    }
}
