package com.biblioteca.api.repositories;

import com.biblioteca.api.models.Prestamo;
import com.biblioteca.api.models.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {
    
    // Método personalizado para buscar si un libro específico tiene un préstamo activo
    boolean existsByLibroAndFechaDevolucionIsNull(Libro libro);
}