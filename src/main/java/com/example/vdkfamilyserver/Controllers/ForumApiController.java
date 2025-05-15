package com.example.vdkfamilyserver.Controllers;

import com.example.vdkfamilyserver.Annotations.CurrentUser;
import com.example.vdkfamilyserver.DTO.Data.Forum.ForumAttachmentDTO;
import com.example.vdkfamilyserver.DTO.Data.Forum.ForumPostDTO;
import com.example.vdkfamilyserver.DTO.Data.Forum.ForumTopicDTO;
import com.example.vdkfamilyserver.Models.Forum.ForumAttachment;
import com.example.vdkfamilyserver.Models.Forum.ForumPost;
import com.example.vdkfamilyserver.Models.Forum.ForumTopic;
import com.example.vdkfamilyserver.Repositories.Forum.ForumAttachmentRepository;
import com.example.vdkfamilyserver.Repositories.Forum.ForumPostRepository;
import com.example.vdkfamilyserver.Repositories.Forum.ForumTopicRepository;
import com.example.vdkfamilyserver.Models.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/forum")
@RequiredArgsConstructor
public class ForumApiController {

    private final ForumTopicRepository topicRepository;
    private final ForumPostRepository postRepository;
    private final ForumAttachmentRepository attachmentRepository;

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/png", "image/jpeg", "image/webp", "application/pdf"
    );

    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "pdf", "webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private void validateAttachment(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Файл не может быть пустым");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Файл слишком большой (макс. 5 MB). Текущий размер: " + file.getSize() / (1024 * 1024) + "MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Недопустимый тип файла: " + contentType + ". Разрешены: " + String.join(", ", ALLOWED_CONTENT_TYPES));
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Имя файла не может быть пустым");
        }
        String ext = FilenameUtils.getExtension(originalFilename);
        if (ext == null || !ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Неверное расширение файла: ." + ext + ". Разрешены: " + String.join(", ", ALLOWED_EXTENSIONS));
        }
    }

    private ForumPostDTO toDto(ForumPost post, HttpServletRequest request) {
        return ForumPostDTO.builder()
            .id(post.getId())
            .author(post.getUser().getFirstName() + " " + post.getUser().getLastName())
            .content(post.getContent())
            .createdAt(post.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .editedAt(post.getEditedAt() != null ? post.getEditedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
            .parentId(post.getParent() != null ? post.getParent().getId() : null)
            .attachments(
                Optional.ofNullable(post.getAttachments()).stream().flatMap(Collection::stream)
                    .map(att -> ForumAttachmentDTO.builder()
                        .id(att.getId())
                        .type(att.getType().name())
                        .url(prependServerUrl(request, att.getUrl()))
                        .preview(att.getPreview())
                        .build())
                    .collect(Collectors.toList())
            )
            .build();
    }

    private void saveAttachmentsForPost(ForumPost post, List<MultipartFile> files) throws IOException {
        if (files == null || files.isEmpty()) {
            return;
        }

        Path postDir = Paths.get("uploads/forum", post.getId().toString());
        Files.createDirectories(postDir);

        List<ForumAttachment> savedAttachments = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            validateAttachment(file);

            String originalFilename = file.getOriginalFilename();
            String filename = UUID.randomUUID().toString() + "_" + (originalFilename != null ? originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_") : "file");
            Path destination = postDir.resolve(filename);
            file.transferTo(destination);

            String contentType = file.getContentType();
            ForumAttachment.Type type = (contentType != null && contentType.toLowerCase().startsWith("image/"))
                    ? ForumAttachment.Type.IMAGE
                    : ForumAttachment.Type.FILE;

            ForumAttachment attachment = ForumAttachment.builder()
                    .post(post)
                    .type(type)
                    .url("/uploads/forum/" + post.getId() + "/" + filename)
                    .build();
            savedAttachments.add(attachmentRepository.save(attachment));
        }
        post.setAttachments(savedAttachments);
    }


    // --- GET: список тем ---
    @GetMapping("/topics")
    public ResponseEntity<List<ForumTopicDTO>> getTopics() {
        List<ForumTopicDTO> result = topicRepository.findAll().stream()
                .map(topic -> ForumTopicDTO.builder()
                        .id(topic.getId())
                        .title(topic.getTitle())
                        .author(topic.getUser().getFirstName() + " " + topic.getUser().getLastName())
                        .createdAt(topic.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .postCount(topic.getPosts().size())
                        .build()
                ).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/topics/{id}")
    public ResponseEntity<?> getPostsByTopic(@PathVariable Long id, HttpServletRequest request) {
        ForumTopic topic = topicRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Тема с ID " + id + " не найдена"));

        List<ForumPost> posts = postRepository.findAllWithAttachmentsByTopicId(id);

        List<ForumPostDTO> result = posts.stream()
                .map(post -> toDto(post, request))
                .toList();

        return ResponseEntity.ok(Map.of(
                "topicId", topic.getId(),
                "topicTitle", topic.getTitle(),
                "posts", result
        ));
    }

    // --- POST: ответ в тему ---
    @PostMapping(value = "/topics/{id}/reply", consumes = "multipart/form-data")
    public ResponseEntity<?> replyToTopicWithAttachments(
            @PathVariable Long id,
            @RequestParam("content") String content,
            @RequestParam(value = "parentId", required = false) Long parentId,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @CurrentUser User user,
            HttpServletRequest request
    ) throws IOException {
        ForumTopic topic = topicRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Тема с ID " + id + " не найдена для ответа"));

        ForumPost parentPost = null;
        if (parentId != null) {
            parentPost = postRepository.findById(parentId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Родительский пост с ID " + parentId + " не найден"));
            if (!parentPost.getTopic().getId().equals(topic.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Родительский пост принадлежит другой теме.");
            }
        }

        if (content == null || content.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Содержание поста не может быть пустым.");
        }

        ForumPost post = ForumPost.builder()
                .topic(topic)
                .user(user)
                .parent(parentPost)
                .content(content)
                .build();
        ForumPost savedPost = postRepository.save(post);

        saveAttachmentsForPost(savedPost, files);

        ForumPost postWithAttachments = postRepository.findById(savedPost.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось получить созданный пост"));

        ForumPostDTO createdPostDto = toDto(postWithAttachments, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Ответ успешно добавлен",
                "post", createdPostDto
        ));
    }


    // --- POST: создать тему ---
    @PostMapping(value = "/topics", consumes = "multipart/form-data")
    public ResponseEntity<?> createTopic(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @CurrentUser User user,
            HttpServletRequest request
    ) throws IOException {
        if (title == null || title.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Заголовок темы не может быть пустым.");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Содержание первого поста не может быть пустым.");
        }

        ForumTopic topic = ForumTopic.builder()
                .title(title)
                .user(user)
                .build();
        ForumTopic savedTopic = topicRepository.save(topic);

        ForumPost initialPost = ForumPost.builder()
                .topic(savedTopic)
                .user(user)
                .content(content)
                .build();
        ForumPost savedInitialPost = postRepository.save(initialPost);

        saveAttachmentsForPost(savedInitialPost, files);

        // For response, similar to reply, we might want to return DTOs
        ForumTopicDTO topicDto = ForumTopicDTO.builder()
                .id(savedTopic.getId())
                .title(savedTopic.getTitle())
                .author(savedTopic.getUser().getFirstName() + " " + savedTopic.getUser().getLastName())
                .createdAt(savedTopic.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .postCount(1) // Initial post count
                .build();

        // Refresh initialPost to include attachments for the DTO
        ForumPost initialPostWithAttachments = postRepository.findById(savedInitialPost.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось получить созданный пост темы"));
        ForumPostDTO initialPostDto = toDto(initialPostWithAttachments, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Тема успешно создана",
                "topic", topicDto,
                "initialPost", initialPostDto
        ));
    }

    private String prependServerUrl(HttpServletRequest request, String relativePath) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        return baseUrl + relativePath;
    }
}

