package com.example.vdkfamilyserver.Repositories;

import com.example.vdkfamilyserver.Models.User;
import com.example.vdkfamilyserver.Models.UserData;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface UserDataRepository extends JpaRepository<UserData, Long> {
    List<UserData> findByUser(User user);
    Page<UserData> findByUser(User user, Pageable pageable);
}