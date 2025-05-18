package com.example.vdkfamilyserver.Controllers.Proxy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/embed")
public class PreviewController {

    @GetMapping("/preview")
    public ResponseEntity<Map<String, String>> getPreview(@RequestParam String url) {
        try {
            String host = detectHost(url);
            String image;
            String title;
            if (host.equals("TikTok")){
                try {
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://tikwm.com/api/?url=" + URLEncoder.encode(url, StandardCharsets.UTF_8)))
                            .header("accept", "application/json")
                            .build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode root = mapper.readTree(response.body());

                    JsonNode data = root.path("data");
                    if (data.isMissingNode()) {
                        return ResponseEntity.badRequest().body(Map.of("error", "Не удалось получить превью"));
                    }

                    image = data.path("cover").asText("");
                    title = data.path("title").asText("Видео TikTok");

                    return ResponseEntity.ok(Map.of(
                            "image", image,
                            "title", title,
                            "host", "TikTok"
                    ));
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Ошибка: " + e.getMessage()));
                }
            }
            else if (host.equals("VK")){
                try {
                    // Поддержка vk.com и vkvideo.ru
                    Pattern pattern = Pattern.compile("video([\\-\\d]+)_([\\d]+)");
                    Matcher matcher = pattern.matcher(url);
                    if (!matcher.find()) {
                        return ResponseEntity.badRequest().body(Map.of("error", "Не удалось извлечь параметры видео"));
                    }

                    String oid = matcher.group(1);
                    String id = matcher.group(2);

                    // Пробуем два варианта
                    List<String> extUrls = List.of(
                            String.format("https://vk.com/video_ext.php?oid=%s&id=%s&hd=1&embed=1", oid, id),
                            String.format("https://vkvideo.ru/video_ext.php?oid=%s&id=%s&hd=1&embed=1", oid, id)
                    );

                    for (String extUrl : extUrls) {
                        try {
                            Document doc = Jsoup.connect(extUrl)
                                    .userAgent("Mozilla/5.0")
                                    .timeout(10000)
                                    .get();

                            String poster = doc.select("video").attr("poster");
                            if (poster != null && !poster.isEmpty()) {
                                return ResponseEntity.ok(Map.of(
                                        "image", poster,
                                        "title", "Видео ВКонтакте",
                                        "host", "VK"
                                ));
                            }
                        } catch (IOException ignored) {
                            // просто переходим к следующей попытке
                        }
                    }

                    return ResponseEntity.badRequest().body(Map.of("error", "Не удалось получить превью VK"));

                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Ошибка VK: " + e.getMessage()));
                }
            }
            else {
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0") // помогает для TikTok/VK
                        .timeout(10000)
                        .get();

                image = doc.select("meta[property=og:image]").attr("content");
                title = doc.select("meta[property=og:title]").attr("content");
            }

            if (image.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Не удалось извлечь превью"));
            }

            Map<String, String> preview = new HashMap<>();
            preview.put("image", image);
            preview.put("title", title);
            preview.put("host", host);

            return ResponseEntity.ok(preview);

        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ошибка загрузки: " + e.getMessage()));
        }
    }

    private String detectHost(String url) {
        if (url.contains("youtube.com") || url.contains("youtu.be")) return "YouTube";
        if (url.contains("rutube.ru")) return "Rutube";
        if (url.contains("tiktok.com")) return "TikTok";
        if (url.contains("vk.com")) return "VK";
        return "Видео";
    }
}

