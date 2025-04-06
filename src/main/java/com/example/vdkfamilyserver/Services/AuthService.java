package com.example.vdkfamilyserver.Services;

import com.example.vdkfamilyserver.Models.User;
import com.example.vdkfamilyserver.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User login(String phoneNumber, String password) {
        return userRepository.findByPhoneNumber(phoneNumber)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
    }
}
