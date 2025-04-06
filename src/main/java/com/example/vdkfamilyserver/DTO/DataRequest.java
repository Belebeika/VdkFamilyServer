package com.example.vdkfamilyserver.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DataRequest {
    @NotBlank(message = "Data cannot be empty")
    private String data;
}
