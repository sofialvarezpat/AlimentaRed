package com.example.AlimentaRed.config;

import com.example.AlimentaRed.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (cabecera != null && cabecera.startsWith("Bearer ")) {
            try {
                Claims claims = jwtService.leer(cabecera.substring(7));
                Long id = ((Number) claims.get("id")).longValue();

                usuarioRepository.findById(id)
                        .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                        .ifPresent(u -> {
                            UsuarioAutenticado principal =
                                    new UsuarioAutenticado(u.getId(), u.getEmail(), u.getRol());
                            UsernamePasswordAuthenticationToken autenticacion =
                                    new UsernamePasswordAuthenticationToken(
                                            principal, null,
                                            List.of(new SimpleGrantedAuthority("ROLE_" + u.getRol().name())));
                            SecurityContextHolder.getContext().setAuthentication(autenticacion);
                        });
            } catch (JwtException | IllegalArgumentException | ClassCastException | NullPointerException e) {
                // Token inválido o vencido: la petición sigue sin autenticar y recibirá un 401
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}