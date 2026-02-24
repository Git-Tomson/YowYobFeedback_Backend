package com.yowyob.feedback.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
public class SimpleHealthController {

    private final DatabaseClient database_client;

    @GetMapping
    public Mono<Map<String, String>> health() {
        return database_client.sql("SELECT 1")
                .fetch()
                .one()
                .map(result -> Map.of(
                        "status", "UP",
                        "database", "connected"
                ))
                .onErrorResume(error -> Mono.just(Map.of(
                        "status", "DOWN",
                        "error", error.getMessage()
                )));
    }
}
