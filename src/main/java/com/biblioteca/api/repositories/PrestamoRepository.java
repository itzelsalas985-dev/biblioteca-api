package com.biblioteca.api.repositories;

import com.biblioteca.api.models.Libro;
import com.biblioteca.api.models.Prestamo;
import com.biblioteca.api.models.Socio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    // RF04: saber si un libro tiene un préstamo activo (sin fecha de devolución)
    boolean existsByLibroAndFechaDevolucionIsNull(Libro libro);

    // Saber si un socio tiene préstamos sin devolver
    boolean existsBySocioAndFechaDevolucionIsNull(Socio socio);

    List<Prestamo> findBySocio(Socio socio);
}
