package com.biblioteca.api.repositories;

import com.biblioteca.api.models.Editorial;
import com.biblioteca.api.models.Libro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibroRepository extends JpaRepository<Libro, Long> {

    // Sirve para no borrar una editorial que todavía tiene libros
    boolean existsByEditorial(Editorial editorial);
}
