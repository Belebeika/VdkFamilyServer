package com.example.vdkfamilyserver.Config;

import com.example.vdkfamilyserver.Models.User;
import com.example.vdkfamilyserver.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.phone}")
    private String phone;

    @Value("${admin.password}")
    private String password;

    @Override
    public void run(String... args) {

        if (userRepository.findByPhoneNumber(phone).isEmpty()) {
            User admin = User.builder()
                    .firstName("Admin")
                    .lastName("User")
                    .phoneNumber(phone)
                    .password(passwordEncoder.encode(password))
                    .role(User.Role.ADMIN)
                    .active(true)
                    .married(false)
                    .build();

            userRepository.save(admin);
            System.out.println("✅ Админ создан: " + phone);
        } else {
            System.out.println("ℹ️ Админ уже существует: " + phone);
        }
    }
}

