package com.slotbooking.modules.payment.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

/**
 * Service wrapper implementation for Razorpay API calls.
 */
@Service
public class RazorpayServiceImpl implements RazorpayService {

    private RazorpayClient razorpayClient;

    @Value("${RAZORPAY_KEY_ID}")
    private String keyId;

    @Value("${RAZORPAY_KEY_SECRET}")
    private String keySecret;

    @PostConstruct
    public void init() throws Exception {
        if (keyId != null && !keyId.isBlank() && keySecret != null && !keySecret.isBlank() && !"YOUR_TEST_SECRET".equals(keySecret)) {
            this.razorpayClient = new RazorpayClient(keyId, keySecret);
        }
    }

    @Override
    public com.razorpay.Order createOrder(JSONObject orderRequest) throws Exception {
        if ("YOUR_TEST_SECRET".equals(keySecret) || keySecret == null || keySecret.isBlank()) {
            String mockOrderId = "order_mock_" + System.currentTimeMillis();
            JSONObject mockOrderJson = new JSONObject();
            mockOrderJson.put("id", mockOrderId);
            mockOrderJson.put("amount", orderRequest.optInt("amount", 0));
            mockOrderJson.put("currency", orderRequest.optString("currency", "INR"));
            mockOrderJson.put("status", "created");
            return new com.razorpay.Order(mockOrderJson);
        }

        if (razorpayClient == null) {
            throw new IllegalStateException("RazorpayClient is not initialized. Please check credentials.");
        }
        return razorpayClient.orders.create(orderRequest);
    }

    @Override
    public void refundPayment(String paymentId, JSONObject refundRequest) throws Exception {
        if (razorpayClient == null) {
            throw new IllegalStateException("RazorpayClient is not initialized. Please check credentials.");
        }
        razorpayClient.payments.refund(paymentId, refundRequest);
    }
}
