package com.biblioteca.api.controllers;

import com.biblioteca.api.models.Prestamo;
import com.biblioteca.api.models.Libro;
import com.biblioteca.api.repositories.PrestamoRepository;
import com.biblioteca.api.repositories.LibroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private LibroRepository libroRepository;

    @GetMapping
    public List<Prestamo> listarPrestamos() {
        return prestamoRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> registrarPrestamo(@RequestBody Prestamo prestamo) {
        // Validar que el libro exista
        if (prestamo.getLibro() == null || prestamo.getLibro().getId() == null) {
            return ResponseEntity.badRequest().body("Debe especificar un libro para el préstamo.");
        }

        Libro libro = libroRepository.findById(prestamo.getLibro().getId()).orElse(null);
        if (libro == null) {
            return ResponseEntity.badRequest().body("El libro no existe.");
        }

        // RF04: Validar la disponibilidad del libro (si ya tiene un préstamo sin fecha de devolución)
        boolean libroOcupado = prestamoRepository.existsByLibroAndFechaDevolucionIsNull(libro);
        if (libroOcupado) {
            return ResponseEntity.badRequest().body("No se puede prestar el libro porque ya tiene un préstamo activo.");
        }

        // Si está libre, guardamos el préstamo
        Prestamo nuevoPrestamo = prestamoRepository.save(prestamo);
        return ResponseEntity.ok(nuevoPrestamo);
    }
}
