package com.example.AlimentaRed.Exception;

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String mensaje) {
        super(mensaje);
    }
}