package com.suraj.Customer_Portal_29.entity;

import java.time.LocalDateTime;

public class PreviewToken {

    private final String token;
    private final String ownerEmail;
    private final String solarRecordId;
    private final PdfDocType docType;
    private final byte[] pdfBytes;
    private final LocalDateTime expiresAt;

    public PreviewToken(String token, String ownerEmail, String solarRecordId,
                        PdfDocType docType, byte[] pdfBytes, LocalDateTime expiresAt) {
        this.token = token;
        this.ownerEmail = ownerEmail;
        this.solarRecordId = solarRecordId;
        this.docType = docType;
        this.pdfBytes = pdfBytes;
        this.expiresAt = expiresAt;
    }

    public String getToken() { return token; }
    public String getOwnerEmail() { return ownerEmail; }
    public String getSolarRecordId() { return solarRecordId; }
    public PdfDocType getDocType() { return docType; }
    public byte[] getPdfBytes() { return pdfBytes; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public boolean isExpired() { return LocalDateTime.now().isAfter(expiresAt); }
}