package com.example.vdkfamilyserver.Models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "temp_users")
@Data
public class TempUser {

    @Id
    private String phoneNumber; // Будет использоваться как ID

    @Column(nullable = false)
    private String verificationCode;

    @Column(nullable = false)
    private LocalDateTime codeExpiryTime;

    @Column(nullable = false)
    private int attemptsLeft = 3; // Количество оставшихся попыток ввода кода

    @Column(nullable = false)
    public boolean isVerified = false;
}
