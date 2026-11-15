package com.suraj.Customer_Portal_29.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class URL_View_Request_Dto {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "URL is required")
    @Size(max = 1000, message = "URL must not exceed 1000 characters")
    private String url;

    private Integer sortOrder;
}