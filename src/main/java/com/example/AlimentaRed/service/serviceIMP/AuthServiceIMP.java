package com.example.AlimentaRed.service.serviceIMP;

import com.example.AlimentaRed.Exception.ResourceNotFoundException;
import com.example.AlimentaRed.config.JwtService;
import com.example.AlimentaRed.dto.AuthResponse;
import com.example.AlimentaRed.dto.LoginRequest;
import com.example.AlimentaRed.dto.RegisterRequest;
import com.example.AlimentaRed.dto.UsuarioResponse;
import com.example.AlimentaRed.entity.Usuario;
import com.example.AlimentaRed.repository.UsuarioRepository;
import com.example.AlimentaRed.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceIMP implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AuthResponse registrar(RegisterRequest request) {
        // Implementa la lógica de registro según tus entidades (Usuario + Organización)
        // Ejemplo base:
        Usuario usuario = new Usuario();
        usuario.setEmail(request.email());
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setNombre(request.nombre());
        usuario.setRol(request.rol());
        usuario.setActivo(true);

        Usuario guardado = usuarioRepository.save(usuario);
        String token = jwtService.generarToken(guardado);

        return new AuthResponse(token, "Bearer", UsuarioResponse.desde(guardado));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + request.email()));

        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new RuntimeException("El usuario se encuentra inactivo");
        }

        String token = jwtService.generarToken(usuario);
        return new AuthResponse(token, "Bearer", UsuarioResponse.desde(usuario));
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse perfil(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id " + usuarioId));
        return UsuarioResponse.desde(usuario);
    }

    @Override
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioResponse::desde)
                .toList();
    }
}