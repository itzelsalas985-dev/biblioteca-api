package com.biblioteca.api.controllers;

import com.biblioteca.api.models.Editorial;
import com.biblioteca.api.repositories.EditorialRepository;
import com.biblioteca.api.repositories.LibroRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

// Catálogo del administrador: altas, bajas, cambios y consultas de editoriales.
@RestController
@RequestMapping("/api/editoriales")
public class EditorialController {

    private final EditorialRepository editorialRepository;
    private final LibroRepository libroRepository;

    public EditorialController(EditorialRepository editorialRepository, LibroRepository libroRepository) {
        this.editorialRepository = editorialRepository;
        this.libroRepository = libroRepository;
    }

    // Consultar todas las editoriales
    @GetMapping
    public List<Editorial> getAllEditoriales() {
        return editorialRepository.findAll();
    }

    // Consultar una editorial por ID
    @GetMapping("/{id}")
    public Editorial getEditorialById(@PathVariable Long id) {
        return buscar(id);
    }

    // Registrar una editorial
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Editorial createEditorial(@RequestBody Editorial editorial) {
        editorial.setId(null);
        return editorialRepository.save(editorial);
    }

    // Actualizar una editorial
    @PutMapping("/{id}")
    public Editorial updateEditorial(@PathVariable Long id, @RequestBody Editorial datos) {
        Editorial editorial = buscar(id);
        editorial.setNombre(datos.getNombre());
        editorial.setPais(datos.getPais());
        return editorialRepository.save(editorial);
    }

    // Eliminar una editorial (solo si ya no tiene libros)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEditorial(@PathVariable Long id) {
        Editorial editorial = buscar(id);
        if (libroRepository.existsByEditorial(editorial)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar la editorial porque tiene libros registrados.");
        }
        editorialRepository.delete(editorial);
    }

    private Editorial buscar(Long id) {
        return editorialRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Editorial no encontrada con id " + id));
    }
}
