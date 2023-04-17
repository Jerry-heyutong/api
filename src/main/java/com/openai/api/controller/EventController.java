package com.openai.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author 25267
 * @Auther: HeYuTong
 * @Date: 2023/4/11 18:03
 * @Description:
 */
@RestController
@RequestMapping("/v1/event")
@Slf4j
public class EventController {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @GetMapping("/events")
    public SseEmitter handle() {
        SseEmitter emitter = new SseEmitter();
        emitter.onCompletion(() ->
                emitters.remove(emitter)
        );
        emitter.onError((Throwable t) -> {
            // Handle the error, e.g. logging or sending an error response
            log.error("Error occurred during server-sent events:", t);
            try {
                // Try to send an error message to the client
                emitter.send("An error occurred!");
            } catch (IOException e) {
                log.error("Failed to send error message:", e);
            }
        });
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        return emitter;
    }

    @PostMapping("/events")
    public ResponseEntity<Void> sendEvents(@RequestBody String message) {
        emitters.forEach(emitter -> {
            try {
                emitter.send(message, MediaType.TEXT_PLAIN);
            } catch (Exception e) {
                emitter.complete();
            }
        });
        return ResponseEntity.ok().build();
    }
}