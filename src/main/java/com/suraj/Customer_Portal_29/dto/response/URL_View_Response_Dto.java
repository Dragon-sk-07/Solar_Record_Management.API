package com.suraj.Customer_Portal_29.dto.response;

import lombok.Data;

@Data
public class URL_View_Response_Dto {
    private Long id;
    private String title;
    private String url;
    private Integer sortOrder;
    private Long ownerId;
    private boolean global;
}