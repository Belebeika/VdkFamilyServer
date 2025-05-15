package com.example.vdkfamilyserver.Repositories;

import com.example.vdkfamilyserver.Models.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {
    List<Banner> findTop5ByOrderByUploadedAtDesc();
}
