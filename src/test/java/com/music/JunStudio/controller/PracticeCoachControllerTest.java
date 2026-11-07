package com.music.JunStudio.controller;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Plain unit tests: the controller is pointed at a throwaway local HTTP server standing in for practice_helper. */
class PracticeCoachControllerTest {

    private HttpServer server;
    private final AtomicReference<String> lastPath = new AtomicReference<>();
    private final AtomicReference<String> lastBody = new AtomicReference<>();
    private volatile int status = 200;
    private volatile String reply = "{\"ok\":true}";

    @BeforeEach
    void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api", ex -> {
            lastPath.set(ex.getRequestURI().getPath());
            lastBody.set(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.ISO_8859_1));
            byte[] out = reply.getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(status, out.length);
            ex.getResponseBody().write(out);
            ex.close();
        });
        server.start();
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    private PracticeCoachController controller(String url) {
        return new PracticeCoachController(url, 5);
    }

    private String url() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @Test
    void audioUploadIsForwardedAsMultipart() {
        var file = new MockMultipartFile("file", "take.wav", "audio/wav", "RIFFdata".getBytes());
        ResponseEntity<String> r = controller(url()).audio(file);
        assertEquals(200, r.getStatusCode().value());
        assertEquals("{\"ok\":true}", r.getBody());
        assertEquals("/api/audio", lastPath.get());
        assertTrue(lastBody.get().contains("filename=\"take.wav\"") && lastBody.get().contains("RIFFdata"));
    }

    @Test
    void scoreJsonIsForwardedUnderJsonField() {
        var json = new MockMultipartFile("json", "s.json", "application/json", "{}".getBytes());
        controller(url()).score(null, json);
        assertEquals("/api/score", lastPath.get());
        assertTrue(lastBody.get().contains("name=\"json\""));
    }

    @Test
    void compareForwardsJsonBody() {
        ResponseEntity<String> r = controller(url()).compare("{\"score\":{},\"audio\":{}}");
        assertEquals(200, r.getStatusCode().value());
        assertEquals("/api/compare", lastPath.get());
        assertEquals("{\"score\":{},\"audio\":{}}", lastBody.get());
    }

    @Test
    void upstreamErrorsPassThroughWithTheirStatus() {
        status = 422;
        reply = "{\"error\":\"Could not read the sheet music.\"}";
        var file = new MockMultipartFile("file", "a.png", "image/png", "x".getBytes());
        ResponseEntity<String> r = controller(url()).score(file, null);
        assertEquals(422, r.getStatusCode().value());
        assertEquals(reply, r.getBody());
    }

    @Test
    void emptyUploadIsRejected() {
        var empty = new MockMultipartFile("file", "a.wav", "audio/wav", new byte[0]);
        assertEquals(400, controller(url()).audio(empty).getStatusCode().value());
    }

    @Test
    void unreachableServiceGives503Json() {
        var file = new MockMultipartFile("file", "a.wav", "audio/wav", "x".getBytes());
        ResponseEntity<String> r = controller("http://127.0.0.1:1").audio(file);
        assertEquals(503, r.getStatusCode().value());
        assertTrue(r.getBody().contains("unavailable"));
    }
}
