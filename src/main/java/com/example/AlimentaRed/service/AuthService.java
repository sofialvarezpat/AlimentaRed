
package com.example.AlimentaRed.service;

import com.example.AlimentaRed.dto.AuthResponse;
import com.example.AlimentaRed.dto.LoginRequest;
import com.example.AlimentaRed.dto.RegisterRequest;
import com.example.AlimentaRed.dto.UsuarioResponse;

import java.util.List;


public interface AuthService {
    AuthResponse registrar(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UsuarioResponse perfil(Long usuarioId);
    List<UsuarioResponse> listarUsuarios();
}