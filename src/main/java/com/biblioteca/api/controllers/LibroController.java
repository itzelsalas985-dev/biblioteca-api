package com.biblioteca.api.controllers;

import com.biblioteca.api.dto.LibroRequest;
import com.biblioteca.api.models.Autor;
import com.biblioteca.api.models.Editorial;
import com.biblioteca.api.models.Libro;
import com.biblioteca.api.repositories.AutorRepository;
import com.biblioteca.api.repositories.EditorialRepository;
import com.biblioteca.api.repositories.LibroRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/libros")
public class LibroController {

    private final LibroRepository libroRepository;
    private final EditorialRepository editorialRepository;
    private final AutorRepository autorRepository;

    public LibroController(LibroRepository libroRepository,
                           EditorialRepository editorialRepository,
                           AutorRepository autorRepository) {
        this.libroRepository = libroRepository;
        this.editorialRepository = editorialRepository;
        this.autorRepository = autorRepository;
    }

    // ---------- CRUD de libros (RF01) ----------

    @GetMapping
    public List<Libro> obtenerLibros() {
        return libroRepository.findAll();
    }

    @GetMapping("/{id}")
    public Libro obtenerLibro(@PathVariable Long id) {
        return buscarLibro(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Libro crearLibro(@RequestBody LibroRequest datos) {
        Libro libro = new Libro();
        copiarDatos(datos, libro);
        return libroRepository.save(libro);
    }

    @PutMapping("/{id}")
    public Libro actualizarLibro(@PathVariable Long id, @RequestBody LibroRequest datos) {
        Libro libro = buscarLibro(id);
        copiarDatos(datos, libro);
        return libroRepository.save(libro);
    }

    // Borra el libro junto con su historial de préstamos
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarLibro(@PathVariable Long id) {
        libroRepository.delete(buscarLibro(id));
    }

    // ---------- Relación muchos a muchos Libro <-> Autor ----------

    @GetMapping("/{libroId}/autores")
    public Set<Autor> autoresDelLibro(@PathVariable Long libroId) {
        return buscarLibro(libroId).getAutores();
    }

    @PostMapping("/{libroId}/autores/{autorId}")
    @Transactional
    public Libro agregarAutor(@PathVariable Long libroId, @PathVariable Long autorId) {
        Libro libro = buscarLibro(libroId);
        libro.getAutores().add(buscarAutor(autorId));
        return libroRepository.save(libro);
    }

    @DeleteMapping("/{libroId}/autores/{autorId}")
    @Transactional
    public Libro quitarAutor(@PathVariable Long libroId, @PathVariable Long autorId) {
        Libro libro = buscarLibro(libroId);
        libro.getAutores().remove(buscarAutor(autorId));
        return libroRepository.save(libro);
    }

    // ---------- Utilidades ----------

    private void copiarDatos(LibroRequest datos, Libro libro) {
        if (datos.titulo() == null || datos.titulo().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El título es obligatorio.");
        }
        libro.setTitulo(datos.titulo().trim());

        Editorial editorial = null;
        if (datos.editorialId() != null) {
            editorial = editorialRepository.findById(datos.editorialId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "La editorial " + datos.editorialId() + " no existe."));
        }
        libro.setEditorial(editorial);

        Set<Autor> autores = new HashSet<>();
        if (datos.autorIds() != null) {
            for (Long autorId : datos.autorIds()) {
                autores.add(autorRepository.findById(autorId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "El autor " + autorId + " no existe.")));
            }
        }
        libro.setAutores(autores);
    }

    private Libro buscarLibro(Long id) {
        return libroRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Libro no encontrado con id " + id));
    }

    private Autor buscarAutor(Long id) {
        return autorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Autor no encontrado con id " + id));
    }
}
