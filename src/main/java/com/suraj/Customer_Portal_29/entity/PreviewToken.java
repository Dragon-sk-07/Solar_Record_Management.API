package com.suraj.Customer_Portal_29.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PreviewToken {

    private final String token;
    private final String ownerEmail;
    private final String solarRecordId;
    private final PdfDocType docType;
    private final byte[] pdfBytes;
    private final LocalDateTime expiresAt;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}