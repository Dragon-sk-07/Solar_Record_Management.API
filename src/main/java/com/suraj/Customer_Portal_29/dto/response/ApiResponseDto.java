package com.suraj.Customer_Portal_29.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponseDto<T> {

    private String message;
    private T data;

}