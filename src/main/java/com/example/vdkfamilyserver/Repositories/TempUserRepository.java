package com.example.vdkfamilyserver.Repositories;

import com.example.vdkfamilyserver.Models.TempUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TempUserRepository extends JpaRepository<TempUser, String> {
    Optional<TempUser> findByPhoneNumber(String phoneNumber);
    void deleteByPhoneNumber(String phoneNumber);
}
