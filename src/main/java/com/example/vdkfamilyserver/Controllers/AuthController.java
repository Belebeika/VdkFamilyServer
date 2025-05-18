package com.example.vdkfamilyserver.Controllers;

import com.example.vdkfamilyserver.DTO.LoginRequest;
import com.example.vdkfamilyserver.Models.User;
import com.example.vdkfamilyserver.Services.AuthService;
import com.example.vdkfamilyserver.Services.JwtService;
import com.example.vdkfamilyserver.Services.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService; // Внедряем сервис токенов

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        User user = authService.login(loginRequest.getPhoneNumber(), loginRequest.getPassword());
        if (user != null) {
            if (!user.isActive()) {
                return ResponseEntity.badRequest().body("Ваша учётная запись была заблокирована");
            }
            UserDetailsImpl userDetails = new UserDetailsImpl(user);
            String token = jwtService.generateToken(
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    )
            );
            return ResponseEntity.ok(new LoginResponse(token));
        } else {
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(HttpServletRequest request) { // Принимаем HttpServletRequest
        try {
            String token = jwtService.resolveToken(request); // Передаем request
            System.out.println("Received token: " + token);

            if (token == null || !jwtService.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            // Извлекаем токен из запроса
            String token = jwtService.resolveToken(request);
            System.out.println("Delete token: " + token);

            if (token == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            jwtService.invalidateToken(token);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

@Getter
class LoginResponse {
    private String token;

    public LoginResponse(String token) {
        this.token = token;
    }
}