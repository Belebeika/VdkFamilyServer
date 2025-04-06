package com.example.vdkfamilyserver.Models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "user") // Исключаем user из логов
public class UserData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String data;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Для удобства работы
    public void setUser(User user) {
        this.user = user;
        user.getUserData().add(this); // Если в User есть список UserData
    }
}