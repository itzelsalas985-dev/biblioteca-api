package com.biblioteca.api.controllers;

import com.biblioteca.api.models.Editorial;
import com.biblioteca.api.repositories.EditorialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/editoriales")
public class EditorialController {

    @Autowired
    private EditorialRepository editorialRepository;

    // GET: Consultar todos
    @GetMapping
    public List<Editorial> getAllEditoriales() {
        return editorialRepository.findAll();
    }

    // GET: Consultar por ID
    @GetMapping("/{id}")
    public Optional<Editorial> getEditorialById(@PathVariable Long id) {
        return editorialRepository.findById(id);
    }

    // POST: Dar de alta (Crear)
    @PostMapping
    public Editorial createEditorial(@RequestBody Editorial editorial) {
        return editorialRepository.save(editorial);
    }

    // PUT: Cambiar (Actualizar)
    @PutMapping("/{id}")
    public Editorial updateEditorial(@PathVariable Long id, @RequestBody Editorial editorialDetails) {
        Editorial editorial = editorialRepository.findById(id).orElseThrow(() -> new RuntimeException("Editorial no encontrada con id " + id));
        editorial.setNombre(editorialDetails.getNombre());
        editorial.setPais(editorialDetails.getPais());
        return editorialRepository.save(editorial);
    }

    // DELETE: Borrar
    @DeleteMapping("/{id}")
    public void deleteEditorial(@PathVariable Long id) {
        editorialRepository.deleteById(id);
    }
}