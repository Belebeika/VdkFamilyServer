package com.example.vdkfamilyserver.Models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "password")
@Builder
public class User {

    public enum Role {
        USER, EDITOR, ADMIN
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column
    private String patronymic; // Необязательное поле

    @Column(nullable = false, unique = true)
    private String phoneNumber; // Для регистрации/авторизации

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Информация о браке
    @Column(nullable = false)
    private boolean married = false;

    // Дети пользователя
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Child> children = new HashSet<>();

    // Метод для добавления ребёнка
    public void addChild(LocalDate birthDate, Child.Gender gender) {
        Child child = Child.builder()
                .birthDate(birthDate)
                .gender(gender)
                .parent(this)
                .build();
        children.add(child);
    }
}