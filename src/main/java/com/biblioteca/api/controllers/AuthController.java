package com.biblioteca.api.controllers;

import com.biblioteca.api.dto.AuthResponse;
import com.biblioteca.api.dto.LoginRequest;
import com.biblioteca.api.dto.RegistroRequest;
import com.biblioteca.api.models.Usuario;
import com.biblioteca.api.repositories.UsuarioRepository;
import com.biblioteca.api.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthController(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          JwtTokenProvider tokenProvider) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    // Registro de un usuario nuevo (bibliotecario). Devuelve el token para entrar directo.
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registro(@RequestBody RegistroRequest datos) {
        if (vacio(datos.nombre()) || vacio(datos.correo()) || vacio(datos.password())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre, correo y contraseña son obligatorios.");
        }
        if (datos.password().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña debe tener al menos 6 caracteres.");
        }
        String correo = datos.correo().trim().toLowerCase();
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese correo.");
        }

        Usuario usuario = usuarioRepository.save(new Usuario(
                datos.nombre().trim(), correo, passwordEncoder.encode(datos.password()), "BIBLIOTECARIO"));
        log.info("Usuario registrado: {}", correo);
        return respuesta(usuario);
    }

    // Inicio de sesión: valida la contraseña con BCrypt y devuelve el JWT
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest datos) {
        String correo = datos.correo() == null ? "" : datos.correo().trim().toLowerCase();
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .filter(u -> datos.password() != null && passwordEncoder.matches(datos.password(), u.getPassword()))
                .orElseThrow(() -> {
                    log.warn("Inicio de sesión fallido para {}", correo);
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos.");
                });
        log.info("Inicio de sesión correcto: {}", correo);
        return respuesta(usuario);
    }

    private AuthResponse respuesta(Usuario usuario) {
        String token = tokenProvider.generarToken(usuario.getCorreo(), usuario.getRol());
        return new AuthResponse(token, "Bearer", usuario.getNombre(), usuario.getCorreo(), usuario.getRol());
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
