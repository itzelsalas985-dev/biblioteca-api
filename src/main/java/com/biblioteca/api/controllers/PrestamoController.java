package com.biblioteca.api.controllers;

import com.biblioteca.api.dto.PrestamoRequest;
import com.biblioteca.api.models.Libro;
import com.biblioteca.api.models.Prestamo;
import com.biblioteca.api.models.Socio;
import com.biblioteca.api.repositories.LibroRepository;
import com.biblioteca.api.repositories.PrestamoRepository;
import com.biblioteca.api.repositories.SocioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {

    private static final int DIAS_DE_PRESTAMO = 14;

    private final PrestamoRepository prestamoRepository;
    private final LibroRepository libroRepository;
    private final SocioRepository socioRepository;

    public PrestamoController(PrestamoRepository prestamoRepository,
                              LibroRepository libroRepository,
                              SocioRepository socioRepository) {
        this.prestamoRepository = prestamoRepository;
        this.libroRepository = libroRepository;
        this.socioRepository = socioRepository;
    }

    @GetMapping
    public List<Prestamo> listarPrestamos() {
        return prestamoRepository.findAll();
    }

    @GetMapping("/{id}")
    public Prestamo obtenerPrestamo(@PathVariable Long id) {
        return buscar(id);
    }

    // RF03 y RF04: registrar un préstamo de un libro disponible a un socio existente
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Prestamo registrarPrestamo(@RequestBody PrestamoRequest datos) {
        if (datos.libroId() == null || datos.socioId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debe especificar el libro y el socio del préstamo.");
        }

        Libro libro = libroRepository.findById(datos.libroId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "El libro no existe."));
        Socio socio = socioRepository.findById(datos.socioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "El socio no existe."));

        // RF04: no se presta un libro que ya tiene un préstamo activo
        if (prestamoRepository.existsByLibroAndFechaDevolucionIsNull(libro)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede prestar el libro porque ya tiene un préstamo activo.");
        }

        LocalDate hoy = LocalDate.now();
        LocalDate limite = datos.fechaLimite() != null ? datos.fechaLimite() : hoy.plusDays(DIAS_DE_PRESTAMO);
        if (limite.isBefore(hoy)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha límite no puede ser anterior a hoy.");
        }

        Prestamo prestamo = new Prestamo();
        prestamo.setLibro(libro);
        prestamo.setSocio(socio);
        prestamo.setFechaPrestamo(hoy);
        prestamo.setFechaLimite(limite);
        return prestamoRepository.save(prestamo);
    }

    // RF05: registrar la devolución; el libro vuelve a estar disponible
    @PutMapping("/{id}/devolver")
    public Prestamo devolver(@PathVariable Long id) {
        Prestamo prestamo = buscar(id);
        if (prestamo.getFechaDevolucion() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este préstamo ya fue devuelto.");
        }
        prestamo.setFechaDevolucion(LocalDate.now());
        return prestamoRepository.save(prestamo);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarPrestamo(@PathVariable Long id) {
        prestamoRepository.delete(buscar(id));
    }

    private Prestamo buscar(Long id) {
        return prestamoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Préstamo no encontrado con id " + id));
    }
}
