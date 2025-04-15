package com.example.vdkfamilyserver.Services;

import com.example.vdkfamilyserver.DTO.UpdateUser;
import com.example.vdkfamilyserver.Models.User;
import com.example.vdkfamilyserver.Repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public boolean existsByPhoneNumber(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    @Transactional
    public void save(User user) throws DataIntegrityViolationException {
        userRepository.save(user);
    }

    @Transactional
    public void update(User user, UpdateUser newuserinfo) throws DataIntegrityViolationException {
        if (newuserinfo.getFirstName() != null) {
            user.setFirstName(newuserinfo.getFirstName());
        }
        if (newuserinfo.getLastName() != null) {
            user.setLastName(newuserinfo.getLastName());
        }
        if (newuserinfo.getIsMarried() != null) {
            user.setMarried(newuserinfo.getIsMarried());
        }
        if (newuserinfo.getPatronymic() != null) {
            user.setPatronymic(newuserinfo.getPatronymic());
        }
        userRepository.save(user);
    }
}
