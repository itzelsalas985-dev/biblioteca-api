package com.biblioteca.api.dto;

// Lo que devuelve el login: el token JWT y los datos básicos del usuario
public record AuthResponse(String token, String tipo, String nombre, String correo, String rol) {
}
