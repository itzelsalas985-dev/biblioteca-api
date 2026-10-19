package com.biblioteca.api.controllers;

import com.biblioteca.api.models.Libro;
import com.biblioteca.api.repositories.LibroRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/libros")
public class LibroController {

    private final LibroRepository libroRepository;

    public LibroController(LibroRepository libroRepository) {
        this.libroRepository = libroRepository;
    }

    @GetMapping
    public List<Libro> obtenerLibros() {
        return libroRepository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Libro crearLibro(@RequestBody Libro libro) {
        return libroRepository.save(libro);
    }
}
