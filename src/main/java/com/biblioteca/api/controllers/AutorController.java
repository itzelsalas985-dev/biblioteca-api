package com.biblioteca.api.controllers;

import com.biblioteca.api.models.Autor;
import com.biblioteca.api.models.Libro;
import com.biblioteca.api.repositories.AutorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

// Catálogo del administrador: altas, bajas, cambios y consultas de autores.
@RestController
@RequestMapping("/api/autores")
public class AutorController {

    private final AutorRepository autorRepository;

    public AutorController(AutorRepository autorRepository) {
        this.autorRepository = autorRepository;
    }

    @GetMapping
    public List<Autor> getAllAutores() {
        return autorRepository.findAll();
    }

    @GetMapping("/{id}")
    public Autor getAutorById(@PathVariable Long id) {
        return buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Autor createAutor(@RequestBody Autor autor) {
        autor.setId(null);
        return autorRepository.save(autor);
    }

    @PutMapping("/{id}")
    public Autor updateAutor(@PathVariable Long id, @RequestBody Autor datos) {
        Autor autor = buscar(id);
        autor.setNombre(datos.getNombre());
        autor.setNacionalidad(datos.getNacionalidad());
        return autorRepository.save(autor);
    }

    // Al borrar un autor primero se quita de sus libros (tabla libro_autor)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void deleteAutor(@PathVariable Long id) {
        Autor autor = buscar(id);
        for (Libro libro : autor.getLibros()) {
            libro.getAutores().remove(autor);
        }
        autorRepository.delete(autor);
    }

    private Autor buscar(Long id) {
        return autorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Autor no encontrado con id " + id));
    }
}
