package com.example.vdkfamilyserver.Controllers;

import com.example.vdkfamilyserver.DTO.Register.PhoneRequest;
import com.example.vdkfamilyserver.DTO.Register.VerificationRequest;
import com.example.vdkfamilyserver.Models.TempUser;
import com.example.vdkfamilyserver.Models.User;
import com.example.vdkfamilyserver.Services.TempUserService;
import com.example.vdkfamilyserver.Services.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/register")
public class RegisterController {
    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);

    private final TempUserService tempUserService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public RegisterController(TempUserService tempUserService,
                              UserService userService,
                              PasswordEncoder passwordEncoder) {
        this.tempUserService = tempUserService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    // Шаг 1: Отправка номера телефона
    @PostMapping("/phone")
    public ResponseEntity<?> registerPhone(@Valid @RequestBody PhoneRequest request) {
        logger.info("Registration attempt for phone: {}", request.getPhoneNumber());

        if (userService.existsByPhoneNumber(request.getPhoneNumber())) {
            return ResponseEntity.badRequest().body("Номер уже зарегистрирован");
        }

        String code = tempUserService.createOrUpdateTempUser(request.getPhoneNumber());
        logger.info("Verification code generated for {}: {}", request.getPhoneNumber(), code);

        return ResponseEntity.ok("Код подтверждения отправлен: ");
    }

    // Шаг 2: Верификация кода
    @PostMapping("/verify")
    public ResponseEntity<?> verifyCode(@Valid @RequestBody VerificationRequest request) {
        logger.info("Verification attempt for phone: {}", request.getPhoneNumber());

        tempUserService.verifyCode(
                request.getPhoneNumber(),
                request.getCode()
        );

        return ResponseEntity.ok("Номер подтверждён");
    }

    // Шаг 3: Завершение регистрации
    @PostMapping("/complete")
    public ResponseEntity<?> completeRegistration(@Valid @RequestBody User user) {
        logger.info("Completing registration for phone: {}", user.getPhoneNumber());

        try {
            TempUser tempUser = tempUserService.getTempUser(user.getPhoneNumber());
            if (tempUser == null || !tempUser.isVerified()) {
                return ResponseEntity.badRequest().body("Сессия регистрации устарела");
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setActive(true);
            user.setRole(User.Role.USER);
            user.setCreatedAt(LocalDateTime.now());

            userService.save(user);
            tempUserService.deleteTempUser(user.getPhoneNumber());

            logger.info("User registered successfully: {}", user.getPhoneNumber());
            return ResponseEntity.ok("Registration completed successfully");
        } catch (DataIntegrityViolationException e) {
            logger.error("Data integrity violation for phone {}: {}", user.getPhoneNumber(), e.getMessage());
            return ResponseEntity.badRequest().body("Номер телефона уже зарегистрирован");
        } catch (Exception e) {
            logger.error("Registration failed for phone {}: {}", user.getPhoneNumber(), e.getMessage());
            return ResponseEntity.internalServerError().body("Registration failed. Please try again.");
        }
    }
}