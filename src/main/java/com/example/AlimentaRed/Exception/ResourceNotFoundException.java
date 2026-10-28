package com.example.AlimentaRed.Exception;

public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException (String mensaje){
        super(mensaje);
    }
}
