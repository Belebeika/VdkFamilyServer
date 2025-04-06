package com.example.vdkfamilyserver.DTO;

import lombok.Getter;
import lombok.Setter;

// Используем Lombok для генерации геттеров/сеттеров
@Getter
@Setter
public class LoginRequest {
    private String phoneNumber;
    private String password;
}