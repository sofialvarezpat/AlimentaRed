package com.example.AlimentaRed.service;

import com.example.AlimentaRed.Exception.BusinessRuleException;
import com.example.AlimentaRed.Exception.DuplicateResourceException;
import com.example.AlimentaRed.Exception.ForbiddenOperationException;
import com.example.AlimentaRed.Exception.ResourceNotFoundException;
import com.example.AlimentaRed.config.JwtService;
import com.example.AlimentaRed.dto.AuthResponse;
import com.example.AlimentaRed.dto.LoginRequest;
import com.example.AlimentaRed.dto.RegisterRequest;
import com.example.AlimentaRed.dto.UsuarioResponse;
import com.example.AlimentaRed.entity.Organizacion;
import com.example.AlimentaRed.entity.Usuario;
import com.example.AlimentaRed.entity.enums.Rol;
import com.example.AlimentaRed.entity.enums.TipoOrganizacion;
import com.example.AlimentaRed.repository.OrganizacionRepository;
import com.example.AlimentaRed.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final OrganizacionRepository organizacionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // Registro público: solo donantes y receptoras
    @Transactional
    public AuthResponse registrar(RegisterRequest request) {
        if (request.rol() != Rol.DONANTE && request.rol() != Rol.RECEPTORA) {
            throw new ForbiddenOperationException(
                    "Solo puedes registrarte como donante o receptora. Los demás roles los asigna un administrador");
        }

        String email = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Ya existe un usuario con ese correo");
        }

        if (request.nombreOrganizacion() == null || request.nombreOrganizacion().isBlank()) {
            throw new BusinessRuleException("El nombre de la organización es obligatorio");
        }

        Organizacion organizacion = new Organizacion();
        organizacion.setNombre(request.nombreOrganizacion().trim());
        organizacion.setTipo(request.rol() == Rol.DONANTE
                ? TipoOrganizacion.DONANTE
                : TipoOrganizacion.RECEPTORA);
        organizacion.setDireccion(request.direccionOrganizacion());
        organizacion.setTelefono(request.telefonoOrganizacion());
        organizacionRepository.save(organizacion);

        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre().trim());
        usuario.setEmail(email);
        // Se guarda el hash BCrypt, nunca la contraseña en texto plano
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setRol(request.rol());
        usuario.setActivo(true);
        usuario.setOrganizacion(organizacion);
        usuarioRepository.save(usuario);

        return new AuthResponse(jwtService.generarToken(usuario), "Bearer", UsuarioResponse.desde(usuario));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        // Mismo mensaje para correo inexistente y contraseña incorrecta, para no revelar qué correos existen
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!Boolean.TRUE.equals(usuario.getActivo())
                || !passwordEncoder.matches(request.password(), usuario.getPassword())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        return new AuthResponse(jwtService.generarToken(usuario), "Bearer", UsuarioResponse.desde(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse perfil(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id " + usuarioId));
        return UsuarioResponse.desde(usuario);
    }
}