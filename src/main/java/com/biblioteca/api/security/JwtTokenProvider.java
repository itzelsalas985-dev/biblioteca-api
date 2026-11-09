package com.biblioteca.api.security; 

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // Clave secreta segura para firmar los tokens (mínimo 256 bits)
    private final SecretKey jwtSecret = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    private final int jwtExpirationInMs = 86400000; // 1 día de duración

    // Generar el token a partir del nombre de usuario
    public String generarToken(String username) {
        Date ahora = new Date();
        Date fechaExpiracion = new Date(ahora.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .subject(username)
                .issuedAt(ahora)
                .expiration(fechaExpiracion)
                .signWith(jwtSecret)
                .compact();
    }

    // Obtener el usuario a partir del token
    public String obtenerUsernameDelToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(jwtSecret)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    // Validar que el token sea correcto y no haya expirado
    public boolean validarToken(String authToken) {
        try {
            Jwts.parser().verifyWith(jwtSecret).build().parseSignedClaims(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("Token JWT inválido: " + e.getMessage());
        }
        return false;
    }
}