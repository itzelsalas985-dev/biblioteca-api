package com.biblioteca.api.controllers;

import com.biblioteca.api.models.Socio;
import com.biblioteca.api.repositories.PrestamoRepository;
import com.biblioteca.api.repositories.SocioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

// Catálogo del administrador: altas, bajas, cambios y consultas de socios.
@RestController
@RequestMapping("/api/socios")
public class SocioController {

    private final SocioRepository socioRepository;
    private final PrestamoRepository prestamoRepository;

    public SocioController(SocioRepository socioRepository, PrestamoRepository prestamoRepository) {
        this.socioRepository = socioRepository;
        this.prestamoRepository = prestamoRepository;
    }

    // GET: Consultar todos
    @GetMapping
    public List<Socio> getAllSocios() {
        return socioRepository.findAll();
    }

    // GET: Consultar por ID
    @GetMapping("/{id}")
    public Socio getSocioById(@PathVariable Long id) {
        return buscar(id);
    }

    // POST: Dar de alta (no se permiten correos duplicados)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Socio createSocio(@RequestBody Socio socio) {
        if (socioRepository.existsByCorreo(socio.getCorreo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un socio con ese correo.");
        }
        socio.setId(null);
        return socioRepository.save(socio);
    }

    // PUT: Cambiar (Actualizar)
    @PutMapping("/{id}")
    public Socio updateSocio(@PathVariable Long id, @RequestBody Socio datos) {
        Socio socio = buscar(id);
        if (socioRepository.existsByCorreoAndIdNot(datos.getCorreo(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un socio con ese correo.");
        }
        socio.setNombre(datos.getNombre());
        socio.setCorreo(datos.getCorreo());
        return socioRepository.save(socio);
    }

    // DELETE: Borrar (solo si no tiene préstamos activos)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSocio(@PathVariable Long id) {
        Socio socio = buscar(id);
        if (prestamoRepository.existsBySocioAndFechaDevolucionIsNull(socio)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar el socio porque tiene préstamos activos.");
        }
        // Se borra su historial de préstamos ya devueltos
        prestamoRepository.deleteAll(prestamoRepository.findBySocio(socio));
        socioRepository.delete(socio);
    }

    private Socio buscar(Long id) {
        return socioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Socio no encontrado con id " + id));
    }
}
