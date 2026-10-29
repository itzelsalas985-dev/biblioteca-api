package com.biblioteca.api.controllers;

import com.biblioteca.api.models.Socio;
import com.biblioteca.api.repositories.SocioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/socios")
public class SocioController {

    @Autowired
    private SocioRepository socioRepository;

    // GET: Consultar todos
    @GetMapping
    public List<Socio> getAllSocios() {
        return socioRepository.findAll();
    }

    // GET: Consultar por ID
    @GetMapping("/{id}")
    public Optional<Socio> getSocioById(@PathVariable Long id) {
        return socioRepository.findById(id);
    }

    // POST: Dar de alta (Crear)
    @PostMapping
    public Socio createSocio(@RequestBody Socio socio) {
        return socioRepository.save(socio);
    }

    // PUT: Cambiar (Actualizar)
    @PutMapping("/{id}")
    public Socio updateSocio(@PathVariable Long id, @RequestBody Socio socioDetails) {
        Socio socio = socioRepository.findById(id).orElseThrow(() -> new RuntimeException("Socio no encontrado con id " + id));
        socio.setNombre(socioDetails.getNombre());
        socio.setCorreo(socioDetails.getCorreo());
        return socioRepository.save(socio);
    }

    // DELETE: Borrar
    @DeleteMapping("/{id}")
    public void deleteSocio(@PathVariable Long id) {
        socioRepository.deleteById(id);
    }
}
