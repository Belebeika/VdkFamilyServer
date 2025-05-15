package com.example.vdkfamilyserver.DTO;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
public class UpdateUser {
    @Size(min = 2, max = 50, message = "Имя должно быть от 2 до 50 символов")
    private String firstName;

    @Size(min = 2, max = 50, message = "Фамилия должна быть от 2 до 50 символов")
    private String lastName;

    @Size(min = 2, max = 50, message = "Отчество должно быть от 2 до 50 символов")
    private String patronymic;

    private Boolean married;
}