package com.biofer.ventas.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({org.springframework.orm.ObjectOptimisticLockingFailureException.class,
        org.springframework.dao.PessimisticLockingFailureException.class})
    public ResponseEntity<ApiError> manejarConcurrencia(Exception exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError(Instant.now(),409,
            "Otro proceso modificó el registro. Reintente con la misma Idempotency-Key",Map.of(),request.getRequestURI()));
    }

    @ExceptionHandler(org.springframework.web.bind.MissingRequestHeaderException.class)
    public ResponseEntity<ApiError> manejarCabecera(org.springframework.web.bind.MissingRequestHeaderException e,HttpServletRequest request) {
        return ResponseEntity.badRequest().body(new ApiError(Instant.now(),400,"Falta la cabecera " + e.getHeaderName(),Map.of(),request.getRequestURI()));
    }

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> manejarArchivo(Exception e,HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(new ApiError(Instant.now(),413,"El archivo supera 10 MB",Map.of(),request.getRequestURI()));
    }

    public record ApiError(
        Instant fecha,
        int estado,
        String mensaje,
        Map<String, String> campos,
        String ruta
    ) {
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> manejarNegocio(
            ResponseStatusException exception,
            HttpServletRequest request) {

        return ResponseEntity
                .status(exception.getStatusCode())
                .body(new ApiError(
                    Instant.now(),
                    exception.getStatusCode().value(),
                    Objects.toString(
                        exception.getReason(),
                        "La operación no pudo completarse"
                    ),
                    Map.of(),
                    request.getRequestURI()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> manejarValidacion(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        Map<String, String> campos = new TreeMap<>();

        exception.getBindingResult().getFieldErrors()
                .forEach(error -> campos.put(
                    error.getField(),
                    Objects.toString(
                        error.getDefaultMessage(),
                        "Valor inválido"
                    )
                ));

        return ResponseEntity.badRequest().body(
            new ApiError(
                Instant.now(),
                400,
                "Revise los campos enviados",
                campos,
                request.getRequestURI()
            )
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> manejarJson(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {

        return ResponseEntity.badRequest().body(
            new ApiError(
                Instant.now(),
                400,
                "JSON inválido: revise tipos, fechas y valores permitidos",
                Map.of(),
                request.getRequestURI()
            )
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> manejarIntegridad(
            DataIntegrityViolationException exception,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            new ApiError(
                Instant.now(),
                409,
                "Registro duplicado o relación de datos inválida",
                Map.of(),
                request.getRequestURI()
            )
        );
    }
}
