package com.example.AlimentaRed.Exception;

public class ForbiddenOperationException extends RuntimeException {
    public ForbiddenOperationException(String mensaje) {
        super(mensaje);
    }
}
