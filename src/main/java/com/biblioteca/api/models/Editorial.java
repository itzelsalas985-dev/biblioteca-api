package com.biblioteca.api.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "editoriales")
public class Editorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String pais;

    // Relación 1 a N: una editorial publica muchos libros.
    // Es el lado "uno"; la llave foránea editorial_id vive en la tabla libros.
    // @JsonIgnore evita el ciclo infinito Editorial -> Libro -> Editorial al convertir a JSON.
    @OneToMany(mappedBy = "editorial")
    @JsonIgnore
    private List<Libro> libros = new ArrayList<>();

    public Editorial() {}

    public Editorial(String nombre, String pais) {
        this.nombre = nombre;
        this.pais = pais;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }

    public List<Libro> getLibros() { return libros; }
    public void setLibros(List<Libro> libros) { this.libros = libros; }
}
