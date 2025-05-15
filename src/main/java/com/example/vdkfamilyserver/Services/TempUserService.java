package com.example.vdkfamilyserver.Services;

import com.example.vdkfamilyserver.Models.TempUser;
import com.example.vdkfamilyserver.Repositories.TempUserRepository;
import com.example.vdkfamilyserver.Exeptions.TempUserNotFoundException;
import com.example.vdkfamilyserver.Exeptions.VerificationAttemptsExhaustedException;
import com.example.vdkfamilyserver.Exeptions.VerificationCodeExpiredException;
import com.example.vdkfamilyserver.Exeptions.InvalidVerificationCodeException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class TempUserService {

    private final TempUserRepository tempUserRepository;

    public String createOrUpdateTempUser(String phoneNumber) {
        String code = generateRandomCode();

        tempUserRepository.findByPhoneNumber(phoneNumber).ifPresentOrElse(
                tempUser -> {
                    tempUser.setVerificationCode(code);
                    tempUser.setCodeExpiryTime(LocalDateTime.now().plusMinutes(5));
                    tempUser.setAttemptsLeft(3);
                    tempUserRepository.save(tempUser);
                },
                () -> {
                    TempUser newTempUser = new TempUser();
                    newTempUser.setPhoneNumber(phoneNumber);
                    newTempUser.setVerificationCode(code);
                    newTempUser.setCodeExpiryTime(LocalDateTime.now().plusMinutes(5));
                    tempUserRepository.save(newTempUser);
                }
        );

        return code;
    }

    public void verifyCode(String phoneNumber, String code) {
        TempUser tempUser = tempUserRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new TempUserNotFoundException(phoneNumber));

        if (tempUser.getAttemptsLeft() <= 0) {
            throw new VerificationAttemptsExhaustedException();
        }

        if (!tempUser.getVerificationCode().equals(code)) {
            tempUser.setAttemptsLeft(tempUser.getAttemptsLeft() - 1);
            tempUserRepository.save(tempUser);
            throw new InvalidVerificationCodeException();
        }

        if (LocalDateTime.now().isAfter(tempUser.getCodeExpiryTime())) {
            throw new VerificationCodeExpiredException();
        }

        tempUser.setVerified(true);
        tempUserRepository.save(tempUser);
    }

    // Получаем временного пользователя
    public TempUser getTempUser(String phoneNumber) {
        return tempUserRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new TempUserNotFoundException(phoneNumber));
    }

    @Transactional
    public void deleteTempUser(String phoneNumber) {
        try {
            tempUserRepository.deleteByPhoneNumber(phoneNumber);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete temp user", e);
        }
    }

    String generateRandomCode() {
        Random random = new Random();
        int code = 1000 + random.nextInt(9000); // Генерация 4-значного числа
        return String.valueOf(code);
    }
}