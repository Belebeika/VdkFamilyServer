package com.example.vdkfamilyserver.Controllers;

import com.example.vdkfamilyserver.DTO.DataRequest;
import com.example.vdkfamilyserver.Models.User;
import com.example.vdkfamilyserver.Models.UserData;
import com.example.vdkfamilyserver.Repositories.UserDataRepository;
import com.example.vdkfamilyserver.Repositories.UserRepository;
import com.example.vdkfamilyserver.Services.JwtService;
import com.example.vdkfamilyserver.Services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/data")
public class DataController {

    @Autowired
    private UserDataRepository userDataRepository;

    @Autowired
    private UserRepository userRepository; // Добавлено

    @Autowired
    private JwtService jwtService;

    @PostMapping
    public ResponseEntity<?> saveData(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody DataRequest request
    ) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getPrincipal();

            // Получаем сущность User из БД
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            UserData userData = new UserData();
            userData.setData(request.getData());
            userData.setUser(user); // Устанавливаем связь

            userDataRepository.save(userData);
            return ResponseEntity.ok().build();

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid token");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserData(@RequestHeader("Authorization") String authHeader) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getPrincipal();

            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<UserData> data = userDataRepository.findByUser(user);
            return ResponseEntity.ok(data);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid token");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Server error: " + e.getMessage());
        }
    }
}