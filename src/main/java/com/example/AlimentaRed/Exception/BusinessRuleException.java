package com.example.AlimentaRed.Exception;

public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String mensaje) {
        super(mensaje);
    }
}