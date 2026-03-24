package com.suraj.Customer_Portal_29.dto.request;

import lombok.Data;

@Data
public class RegisterRequestDto {
    private String name;
    private String email;
    private String mobile;
    private String password;
}
