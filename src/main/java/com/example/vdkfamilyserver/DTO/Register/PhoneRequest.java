package com.example.vdkfamilyserver.DTO.Register;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PhoneRequest {
    @NotBlank
    @Pattern(regexp = "^\\+7[0-9]{10}$", message = "Номер должен быть в формате +7XXXXXXXXXX (11 цифр)")
    private String phoneNumber;
}