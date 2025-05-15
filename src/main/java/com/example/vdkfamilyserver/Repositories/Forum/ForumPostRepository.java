package com.example.vdkfamilyserver.Repositories.Forum;

import com.example.vdkfamilyserver.Models.Forum.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {
    List<ForumPost> findByTopicIdOrderByCreatedAtAsc(Long id);
    @Query("SELECT p FROM ForumPost p LEFT JOIN FETCH p.attachments WHERE p.topic.id = :topicId ORDER BY p.createdAt ASC")
    List<ForumPost> findAllWithAttachmentsByTopicId(@Param("topicId") Long topicId);
}
