package com.example.AlimentaRed.config;

import com.example.AlimentaRed.entity.Usuario;
import com.example.AlimentaRed.entity.enums.Rol;
import com.example.AlimentaRed.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Crear Administrador por defecto si no existe
        if (!usuarioRepository.existsByEmail("admin@alimentared.com")) {
            Usuario admin = new Usuario();
            admin.setNombre("Administrador");
            admin.setEmail("admin@alimentared.com");
            admin.setPassword(passwordEncoder.encode("Admin123*"));
            admin.setRol(Rol.ADMINISTRADOR);
            admin.setActivo(true);
            usuarioRepository.save(admin);
        }

        // Crear Coordinador por defecto si no existe
        if (!usuarioRepository.existsByEmail("coordinador@alimentared.com")) {
            Usuario coordinador = new Usuario();
            coordinador.setNombre("Coordinador");
            coordinador.setEmail("coordinador@alimentared.com");
            coordinador.setPassword(passwordEncoder.encode("Coord123*"));
            coordinador.setRol(Rol.COORDINADOR);
            coordinador.setActivo(true);
            usuarioRepository.save(coordinador);
        }
    }
}
