
package com.biblioteca.api;

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

    // Consultar todas las editoriales
    @GetMapping
    public List<Editorial> getAllEditoriales() {
        return editorialRepository.findAll();
    }

    // Consultar una editorial por ID
    @GetMapping("/{id}")
    public Optional<Editorial> getEditorialById(@PathVariable Long id) {
        return editorialRepository.findById(id);
    }

    // Registrar una editorial
    @PostMapping
    public Editorial createEditorial(@RequestBody Editorial editorial) {
        return editorialRepository.save(editorial);
    }

    // Actualizar una editorial
    @PutMapping("/{id}")
    public Editorial updateEditorial(
            @PathVariable Long id,
            @RequestBody Editorial editorialDetails) {

        Editorial editorial = editorialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Editorial no encontrada con id " + id));

        editorial.setNombre(editorialDetails.getNombre());
        editorial.setPais(editorialDetails.getPais());

        return editorialRepository.save(editorial);
    }

    // Eliminar una editorial
    @DeleteMapping("/{id}")
    public void deleteEditorial(@PathVariable Long id) {
        editorialRepository.deleteById(id);
    }
}
