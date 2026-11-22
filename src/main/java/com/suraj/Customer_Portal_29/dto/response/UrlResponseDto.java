package com.suraj.Customer_Portal_29.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UrlResponseDto {
    private Long id;
    private String title;
    private String url;
    private boolean isDefault;
}