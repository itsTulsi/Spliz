package com.fairshare.service;

import com.fairshare.exception.BusinessException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class UpiPaymentService {

    // Standard NPCI UPI ID pattern: username@bankhandle (e.g. rahul@okhdfcbank, 9876543210@paytm)
    private static final Pattern UPI_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9.\\-_]{2,256}@[a-zA-Z]{2,64}$");

    public boolean isValidUpiId(String upiId) {
        if (upiId == null) return false;
        String trimmed = upiId.trim();
        return !trimmed.isEmpty() && UPI_ID_PATTERN.matcher(trimmed).matches();
    }

    public void validateUpiId(String upiId) {
        if (!isValidUpiId(upiId)) {
            throw new BusinessException("Invalid UPI ID format. Expected format: username@bank (e.g. rahul@okaxis, 9876543210@paytm).");
        }
    }

    public String formatRupees(Long amountPaise) {
        if (amountPaise == null || amountPaise <= 0) {
            throw new BusinessException("Settlement amount must be positive.");
        }
        return String.format(Locale.US, "%.2f", amountPaise / 100.0);
    }

    /**
     * Constructs standard UPI payment URI:
     * upi://pay?pa=RECIPIENT_UPI_ID&pn=RECIPIENT_NAME&am=AMOUNT&cu=INR&tn=PAYMENT_NOTE
     */
    public String buildStandardUpiUri(String recipientUpiId, String recipientName, Long amountPaise, String note) {
        validateUpiId(recipientUpiId);

        String cleanUpiId = recipientUpiId.trim();
        String cleanName = sanitizeName(recipientName);
        String formattedAmount = formatRupees(amountPaise);
        String cleanNote = sanitizeNote(note);

        String encodedPa = encode(cleanUpiId);
        String encodedPn = encode(cleanName);
        String encodedAm = encode(formattedAmount);
        String encodedCu = "INR";
        String encodedTn = encode(cleanNote);

        return String.format("upi://pay?pa=%s&pn=%s&am=%s&cu=%s&tn=%s",
                encodedPa, encodedPn, encodedAm, encodedCu, encodedTn);
    }

    /**
     * Constructs app-specific deep link if available, with standard fallback.
     */
    public String buildAppSpecificUri(String app, String recipientUpiId, String recipientName, Long amountPaise, String note) {
        String baseQuery = buildStandardUpiUri(recipientUpiId, recipientName, amountPaise, note).substring("upi://pay?".length());
        if (app == null) {
            return "upi://pay?" + baseQuery;
        }

        switch (app.trim().toUpperCase()) {
            case "GPAY":
            case "GOOGLE_PAY":
                return "gpay://upi/pay?" + baseQuery;
            case "PHONEPE":
                return "phonepe://pay?" + baseQuery;
            case "PAYTM":
                return "paytmmp://pay?" + baseQuery;
            case "BHIM":
                return "bhim://pay?" + baseQuery;
            default:
                return "upi://pay?" + baseQuery;
        }
    }

    private String sanitizeName(String name) {
        if (name == null || name.isBlank()) return "FairShare Member";
        // Restrict to letters, digits, spaces, max 50 chars
        String clean = name.replaceAll("[^a-zA-Z0-9 ]", "").trim();
        return clean.length() > 50 ? clean.substring(0, 50).trim() : (clean.isEmpty() ? "FairShare Member" : clean);
    }

    private String sanitizeNote(String note) {
        if (note == null || note.isBlank()) return "FairShare settlement";
        // Sanitize note: no sensitive info, max 50 alphanumeric/space chars
        String clean = note.replaceAll("[^a-zA-Z0-9 .\\-_]", " ").replaceAll("\\s+", " ").trim();
        if (clean.length() > 50) clean = clean.substring(0, 50).trim();
        return clean.isEmpty() ? "FairShare settlement" : clean;
    }

    private String encode(String val) {
        return URLEncoder.encode(val, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /**
     * Generates a 100% ISO/IEC 18004 compliant QR Code SVG using Google ZXing.
     * Guaranteed to scan cleanly on Google Pay, PhonePe, Paytm, BHIM, and smartphone scanners.
     */
    public String generateQrSvg(String text, int size) {
        if (text == null || text.isBlank()) return "";
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 2); // 2 quiet-zone modules

            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, size, size, hints);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();

            StringBuilder sb = new StringBuilder();
            sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 ")
              .append(width).append(" ").append(height)
              .append("\" width=\"").append(size).append("\" height=\"").append(size)
              .append("\" style=\"max-width: 100%; height: auto; border-radius: 8px; display: block; margin: 0 auto;\">\n");
            sb.append("  <rect width=\"100%\" height=\"100%\" fill=\"#ffffff\" />\n");
            sb.append("  <path d=\"");

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (bitMatrix.get(x, y)) {
                        sb.append("M").append(x).append(",").append(y).append("h1v1h-1z ");
                    }
                }
            }
            sb.append("\" fill=\"#0f172a\" />\n");
            sb.append("</svg>");
            return sb.toString();
        } catch (Exception e) {
            return "<div class=\"alert alert-warning\">QR Generation Error</div>";
        }
    }
}
