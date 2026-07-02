package com.slotbooking.modules.payment.service;

import com.razorpay.Order;
import org.json.JSONObject;

/**
 * Service wrapper interface for Razorpay API calls to enable clean mocking and testing.
 */
public interface RazorpayService {
    com.razorpay.Order createOrder(org.json.JSONObject orderRequest) throws Exception;
    void refundPayment(String paymentId, org.json.JSONObject refundRequest) throws Exception;
}
