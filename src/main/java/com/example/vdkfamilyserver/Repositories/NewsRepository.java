package com.example.vdkfamilyserver.Repositories;

import com.example.vdkfamilyserver.Models.News;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewsRepository extends JpaRepository<News, Long> {
    List<News> findTop4ByOrderByUploadedAtDesc();
}
