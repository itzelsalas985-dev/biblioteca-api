package com.biblioteca.api.config;

import com.biblioteca.api.models.*;
import com.biblioteca.api.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Set;

// Como H2 vive en memoria, al arrancar se cargan un administrador y datos de ejemplo
// para que el sistema se pueda probar (y presentar) sin capturar todo otra vez.
@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner cargarDatosIniciales(UsuarioRepository usuarios,
                                           EditorialRepository editoriales,
                                           AutorRepository autores,
                                           LibroRepository libros,
                                           SocioRepository socios,
                                           PrestamoRepository prestamos,
                                           PasswordEncoder passwordEncoder) {
        return args -> {
            if (usuarios.count() == 0) {
                usuarios.save(new Usuario("Administrador", "admin@biblioteca.com",
                        passwordEncoder.encode("admin123"), "ADMIN"));
                log.info("Usuario inicial creado: admin@biblioteca.com / admin123");
            }

            if (libros.count() > 0) {
                return;
            }

            Editorial planeta = editoriales.save(new Editorial("Planeta", "España"));
            Editorial penguin = editoriales.save(new Editorial("Penguin Random House", "Estados Unidos"));
            Editorial fce = editoriales.save(new Editorial("Fondo de Cultura Económica", "México"));

            Autor garcia = autores.save(new Autor("Gabriel García Márquez", "Colombiana"));
            Autor rulfo = autores.save(new Autor("Juan Rulfo", "Mexicana"));
            Autor lovecraft = autores.save(new Autor("H. P. Lovecraft", "Estadounidense"));
            Autor gaiman = autores.save(new Autor("Neil Gaiman", "Británica"));
            Autor pratchett = autores.save(new Autor("Terry Pratchett", "Británica"));

            Libro cien = nuevoLibro("Cien años de soledad", penguin, Set.of(garcia));
            Libro paramo = nuevoLibro("Pedro Páramo", fce, Set.of(rulfo));
            Libro cthulhu = nuevoLibro("La llamada de Cthulhu", planeta, Set.of(lovecraft));
            Libro omens = nuevoLibro("Buenos presagios", planeta, Set.of(gaiman, pratchett));
            libros.saveAll(java.util.List.of(cien, paramo, cthulhu, omens));

            Socio itzel = socios.save(new Socio("Itzel Guadalupe Salas", "itzel@correo.com"));
            socios.save(new Socio("Carlos Cabanzo", "carlos@correo.com"));

            Prestamo prestamo = new Prestamo();
            prestamo.setLibro(cthulhu);
            prestamo.setSocio(itzel);
            prestamo.setFechaPrestamo(LocalDate.now());
            prestamo.setFechaLimite(LocalDate.now().plusDays(14));
            prestamos.save(prestamo);

            log.info("Datos de ejemplo cargados");
        };
    }

    private Libro nuevoLibro(String titulo, Editorial editorial, Set<Autor> autores) {
        Libro libro = new Libro(titulo, editorial);
        libro.setAutores(new java.util.HashSet<>(autores));
        return libro;
    }
}
