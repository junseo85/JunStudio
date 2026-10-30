package com.music.JunStudio.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;

import org.springframework.http.client.SimpleClientHttpRequestFactory;

/**
 * Practice Coach: gives students instant note-by-note feedback by comparing a
 * recording against scanned sheet music. The analysis runs in the separate
 * practice_helper Python service; this controller serves the page and proxies
 * its three endpoints so the browser never talks to that service directly and
 * every call goes through JunStudio's login + CSRF protection.
 */
@Controller
public class PracticeCoachController {

    private final RestClient client;

    public PracticeCoachController(
            @Value("${practice.helper.url:http://localhost:5000}") String baseUrl,
            @Value("${practice.helper.timeout-seconds:300}") long timeoutSeconds) {
        // Long read timeout: the first sheet-music scan downloads OMR models.
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
        this.client = RestClient.builder().baseUrl(baseUrl + "/api").requestFactory(factory).build();
    }

    @GetMapping("/practice-coach")
    public String page() {
        return "practice-coach";
    }

    @PostMapping(value = "/practice-coach/api/score", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<String> score(@RequestParam(value = "file", required = false) MultipartFile file,
                                        @RequestParam(value = "json", required = false) MultipartFile json) {
        return forward("/score", file != null ? "file" : "json", file != null ? file : json);
    }

    @PostMapping(value = "/practice-coach/api/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<String> audio(@RequestParam("file") MultipartFile file) {
        return forward("/audio", "file", file);
    }

    @PostMapping(value = "/practice-coach/api/compare", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> compare(@RequestBody String json) {
        try {
            String body = client.post().uri("/compare").contentType(MediaType.APPLICATION_JSON)
                    .body(json).retrieve().body(String.class);
            return jsonResponse(HttpStatusCode.valueOf(200), body);
        } catch (RestClientResponseException e) {
            return jsonResponse(e.getStatusCode(), e.getResponseBodyAsString());
        } catch (RuntimeException e) {
            return unavailable();
        }
    }

    private ResponseEntity<String> forward(String path, String field, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return jsonResponse(HttpStatusCode.valueOf(400), "{\"error\":\"No file uploaded.\"}");
        }
        try {
            MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
            String filename = file.getOriginalFilename();
            parts.add(field, new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return filename;
                }
            });
            String body = client.post().uri(path).contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(parts).retrieve().body(String.class);
            return jsonResponse(HttpStatusCode.valueOf(200), body);
        } catch (RestClientResponseException e) {
            return jsonResponse(e.getStatusCode(), e.getResponseBodyAsString());
        } catch (IOException | RuntimeException e) {
            return unavailable();
        }
    }

    private static ResponseEntity<String> unavailable() {
        return jsonResponse(HttpStatusCode.valueOf(503),
                "{\"error\":\"The Practice Coach analysis service is unavailable. Please try again later.\"}");
    }

    private static ResponseEntity<String> jsonResponse(HttpStatusCode status, String body) {
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
    }
}
