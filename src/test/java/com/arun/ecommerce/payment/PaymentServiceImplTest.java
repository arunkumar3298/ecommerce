package com.arun.ecommerce.payment;

import com.arun.ecommerce.payment.dto.PaymentVerifyRequest;
import com.arun.ecommerce.payment.dto.PaymentVerifyResponse;
import com.arun.ecommerce.order.OrderService;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock private RazorpayClient razorpayClient;  // kept for completeness
    @Mock private OrderService   orderService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private PaymentVerifyRequest verifyRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "keyId",     "rzp_test_key");
        ReflectionTestUtils.setField(paymentService, "keySecret", "rzp_secret");

        verifyRequest = new PaymentVerifyRequest();
        verifyRequest.setOrderId(301L);
        verifyRequest.setRazorpayOrderId("rzp_order_abc123");
        verifyRequest.setRazorpayPaymentId("pay_xyz789");
        verifyRequest.setRazorpaySignature("valid_signature");
    }

    // ── verifyPayment() tests ─────────────────────────────────────

    @Test
    @DisplayName("verifyPayment: valid signature should return success response")
    void verifyPayment_validSignature_shouldReturnSuccess() {
        try (MockedStatic<com.razorpay.Utils> utils =
                     mockStatic(com.razorpay.Utils.class)) {

            utils.when(() -> com.razorpay.Utils
                            .verifyPaymentSignature(any(JSONObject.class), anyString()))
                    .thenReturn(true);

            PaymentVerifyResponse response =
                    paymentService.verifyPayment(verifyRequest);

            assertTrue(response.isSuccess());
            assertEquals("Payment verified successfully!", response.getMessage());
            assertEquals(301L,   response.getOrderId());
            assertEquals("PAID", response.getPaymentStatus());
        }
    }

    @Test
    @DisplayName("verifyPayment: valid signature should call markAsPaid")
    void verifyPayment_validSignature_shouldMarkOrderAsPaid() {
        try (MockedStatic<com.razorpay.Utils> utils =
                     mockStatic(com.razorpay.Utils.class)) {

            utils.when(() -> com.razorpay.Utils
                            .verifyPaymentSignature(any(JSONObject.class), anyString()))
                    .thenReturn(true);

            paymentService.verifyPayment(verifyRequest);

            verify(orderService, times(1)).markAsPaid(301L);
        }
    }

    @Test
    @DisplayName("verifyPayment: invalid signature should return failure response")
    void verifyPayment_invalidSignature_shouldReturnFailure() {
        try (MockedStatic<com.razorpay.Utils> utils =
                     mockStatic(com.razorpay.Utils.class)) {

            utils.when(() -> com.razorpay.Utils
                            .verifyPaymentSignature(any(JSONObject.class), anyString()))
                    .thenReturn(false);

            PaymentVerifyResponse response =
                    paymentService.verifyPayment(verifyRequest);

            assertFalse(response.isSuccess());
            assertTrue(response.getMessage().contains("verification failed"));
            assertEquals("FAILED", response.getPaymentStatus());
        }
    }

    @Test
    @DisplayName("verifyPayment: invalid signature should NOT call markAsPaid")
    void verifyPayment_invalidSignature_shouldNotMarkAsPaid() {
        try (MockedStatic<com.razorpay.Utils> utils =
                     mockStatic(com.razorpay.Utils.class)) {

            utils.when(() -> com.razorpay.Utils
                            .verifyPaymentSignature(any(JSONObject.class), anyString()))
                    .thenReturn(false);

            paymentService.verifyPayment(verifyRequest);

            verify(orderService, never()).markAsPaid(any());
        }
    }

    @Test
    @DisplayName("verifyPayment: exception during verification should throw RuntimeException")
    void verifyPayment_exception_shouldThrowRuntimeException() {
        try (MockedStatic<com.razorpay.Utils> utils =
                     mockStatic(com.razorpay.Utils.class)) {

            utils.when(() -> com.razorpay.Utils
                            .verifyPaymentSignature(any(JSONObject.class), anyString()))
                    .thenThrow(new RuntimeException("Signature error"));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> paymentService.verifyPayment(verifyRequest));

            assertTrue(ex.getMessage().contains("Payment verification error"));
        }
    }
}
