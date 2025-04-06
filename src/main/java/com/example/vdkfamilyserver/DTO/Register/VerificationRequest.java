package com.example.vdkfamilyserver.DTO.Register;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerificationRequest {
    @NotBlank
    private String phoneNumber;

    @NotBlank
    @Size(min = 4, max = 4, message = "Код должен содержать 4 цифры")
    private String code;
}