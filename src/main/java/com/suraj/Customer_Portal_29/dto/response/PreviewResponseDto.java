package com.suraj.Customer_Portal_29.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PreviewResponseDto {
    private String previewToken;
    private String docType;
    private String urlSegment;
    private int costInRupees;
    private long sizeBytes;
    private long expiresInSeconds;
    private String pdfBase64;
}