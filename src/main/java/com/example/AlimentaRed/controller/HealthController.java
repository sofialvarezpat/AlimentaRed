package com.example.AlimentaRed.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Salud", description = "Estado del servicio")
public class HealthController {

    @GetMapping
    @Operation(summary = "Verifica que la API está activa")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "service", "AlimentaRed API",
                "timestamp", LocalDateTime.now().toString()
        );
    }
}