package com.example.vdkfamilyserver.Repositories;

import com.example.vdkfamilyserver.Models.Child;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChildRepository extends JpaRepository<Child, Long> {
}