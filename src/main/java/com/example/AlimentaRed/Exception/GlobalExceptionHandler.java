package com.example.AlimentaRed.Exception;

import com.example.AlimentaRed.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 404: el registro no existe
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> noEncontrado(ResourceNotFoundException ex, HttpServletRequest req) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), req, null);
    }

    // 422: se rompió una regla de negocio
    @ExceptionHandler({BusinessRuleException.class, IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ApiError> reglaDeNegocio(RuntimeException ex, HttpServletRequest req) {
        return construir(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), req, null);
    }

    // 409: el registro ya existe o choca con otro
    @ExceptionHandler({DuplicateResourceException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ApiError> conflicto(RuntimeException ex, HttpServletRequest req) {
        String mensaje = ex instanceof DuplicateResourceException
                ? ex.getMessage()
                : "La operación viola una restricción de datos (registro duplicado o referencia inválida)";
        return construir(HttpStatus.CONFLICT, mensaje, req, null);
    }

    // 403: el usuario no tiene permiso para esta acción
    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<ApiError> prohibido(ForbiddenOperationException ex, HttpServletRequest req) {
        return construir(HttpStatus.FORBIDDEN, ex.getMessage(), req, null);
    }

    // 400: datos inválidos en el cuerpo de la petición (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();
        return construir(HttpStatus.BAD_REQUEST, "Hay datos inválidos en la solicitud", req, detalles);
    }

    // 400: JSON mal formado o valor que no existe en un enum
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> jsonInvalido(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return construir(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud no es válido", req, null);
    }

    // 400: un parámetro tiene el tipo equivocado (por ejemplo, un id con letras)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> tipoInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return construir(HttpStatus.BAD_REQUEST, "El parámetro '" + ex.getName() + "' no es válido", req, null);
    }

    // 500: cualquier otro error inesperado
    @ExceptionHandler(java.lang.Exception.class)
    public ResponseEntity<ApiError> generico(java.lang.Exception ex, HttpServletRequest req) {
        // Errores propios de Spring (ruta inexistente, método no permitido, etc.) conservan su código
        if (ex instanceof ErrorResponse er) {
            HttpStatus status = HttpStatus.valueOf(er.getStatusCode().value());
            return construir(status, status.getReasonPhrase(), req, null);
        }
        log.error("Error no controlado en {}", req.getRequestURI(), ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno. Intenta de nuevo más tarde", req, null);
    }

    private ResponseEntity<ApiError> construir(HttpStatus status, String mensaje,
                                               HttpServletRequest req, List<String> detalles) {
        ApiError cuerpo = new ApiError(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                mensaje,
                req.getRequestURI(),
                detalles
        );
        return ResponseEntity.status(status).body(cuerpo);
    }
}
