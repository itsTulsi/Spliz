package com.fairshare.service;

import com.fairshare.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class UpiPaymentServiceTest {

    private UpiPaymentService upiPaymentService;

    @BeforeEach
    void setUp() {
        upiPaymentService = new UpiPaymentService();
    }

    @Test
    @DisplayName("Should generate valid standard UPI URI with correctly encoded parameters")
    void testStandardUpiUriGeneration() {
        String uri = upiPaymentService.buildStandardUpiUri(
                "priyanshi@okaxis",
                "Priyanshi Sharma",
                60000L,
                "FairShare Goa Trip dinner"
        );

        assertNotNull(uri);
        assertTrue(uri.startsWith("upi://pay?"));
        assertTrue(uri.contains("pa=priyanshi%40okaxis"));
        assertTrue(uri.contains("pn=Priyanshi%20Sharma"));
        assertTrue(uri.contains("am=600.00"));
        assertTrue(uri.contains("cu=INR"));
        assertTrue(uri.contains("tn=FairShare%20Goa%20Trip%20dinner"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "rahul@okhdfcbank",
            "priyanshi@okaxis",
            "amit@ibl",
            "9876543210@paytm",
            "john.doe@ybl",
            "user_name@sbi"
    })
    @DisplayName("Should validate valid UPI IDs")
    void testValidUpiIds(String upiId) {
        assertTrue(upiPaymentService.isValidUpiId(upiId));
        assertDoesNotThrow(() -> upiPaymentService.validateUpiId(upiId));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "   ",
            "invalidupi",
            "@okaxis",
            "user@",
            "user@bank@extra",
            "user with spaces@bank"
    })
    @DisplayName("Should reject invalid UPI IDs")
    void testInvalidUpiIds(String upiId) {
        assertFalse(upiPaymentService.isValidUpiId(upiId));
        assertThrows(BusinessException.class, () -> upiPaymentService.validateUpiId(upiId));
    }

    @Test
    @DisplayName("Should format paise to exact two decimal rupees")
    void testFormatRupees() {
        assertEquals("600.00", upiPaymentService.formatRupees(60000L));
        assertEquals("12.50", upiPaymentService.formatRupees(1250L));
        assertEquals("0.05", upiPaymentService.formatRupees(5L));
        assertEquals("12345.67", upiPaymentService.formatRupees(1234567L));

        assertThrows(BusinessException.class, () -> upiPaymentService.formatRupees(0L));
        assertThrows(BusinessException.class, () -> upiPaymentService.formatRupees(-500L));
        assertThrows(BusinessException.class, () -> upiPaymentService.formatRupees(null));
    }

    @Test
    @DisplayName("Should generate app-specific deep links with fallback")
    void testAppSpecificUpiUris() {
        String gpayUri = upiPaymentService.buildAppSpecificUri("GPAY", "amit@ibl", "Amit", 150000L, "Settlement");
        assertTrue(gpayUri.startsWith("gpay://upi/pay?pa=amit%40ibl"));

        String phonepeUri = upiPaymentService.buildAppSpecificUri("PHONEPE", "amit@ibl", "Amit", 150000L, "Settlement");
        assertTrue(phonepeUri.startsWith("phonepe://pay?pa=amit%40ibl"));

        String paytmUri = upiPaymentService.buildAppSpecificUri("PAYTM", "amit@ibl", "Amit", 150000L, "Settlement");
        assertTrue(paytmUri.startsWith("paytmmp://pay?pa=amit%40ibl"));

        String bhimUri = upiPaymentService.buildAppSpecificUri("BHIM", "amit@ibl", "Amit", 150000L, "Settlement");
        assertTrue(bhimUri.startsWith("bhim://pay?pa=amit%40ibl"));

        String fallbackUri = upiPaymentService.buildAppSpecificUri("UNKNOWN_APP", "amit@ibl", "Amit", 150000L, "Settlement");
        assertTrue(fallbackUri.startsWith("upi://pay?pa=amit%40ibl"));
    }
}
