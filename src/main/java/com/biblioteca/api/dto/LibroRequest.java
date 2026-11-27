package com.biblioteca.api.dto;

import java.util.List;

// Datos que recibe la API para crear o actualizar un libro.
// La editorial y los autores se mandan solo con su ID.
public record LibroRequest(String titulo, Long editorialId, List<Long> autorIds) {
}
