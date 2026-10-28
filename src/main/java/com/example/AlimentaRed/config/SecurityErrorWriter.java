package com.example.AlimentaRed.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.time.LocalDateTime;

public final class SecurityErrorWriter {

    private SecurityErrorWriter() {
    }

    public static void escribir(HttpServletResponse res, HttpStatus status,
                                String mensaje, String path) throws IOException {
        res.setStatus(status.value());
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String pathSeguro = path.replace("\\", "\\\\").replace("\"", "\\\"");
        String json = String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\",\"detalles\":null}",
                LocalDateTime.now(), status.value(), status.getReasonPhrase(), mensaje, pathSeguro);
        res.getWriter().write(json);
    }
}