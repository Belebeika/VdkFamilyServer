package com.example.vdkfamilyserver.Repositories.Forum;

import com.example.vdkfamilyserver.Models.Forum.ForumAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ForumAttachmentRepository extends JpaRepository<ForumAttachment, Long> {
    List<ForumAttachment> findByPostId(Long postId);
    long countByPostId(Long postId);
}
