package com.biblioteca.api.models;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "libros")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String titulo;

    // Relación Muchos a Uno: muchos libros pertenecen a una sola Editorial
    @ManyToOne
    @JoinColumn(name = "editorial_id")
    private Editorial editorial;

    // Relación Muchos a Muchos: un libro puede tener varios autores y un autor varios libros
    @ManyToMany
    @JoinTable(
        name = "libro_autor",
        joinColumns = @JoinColumn(name = "libro_id"),
        inverseJoinColumns = @JoinColumn(name = "autor_id")
    )
    private Set<Autor> autores = new HashSet<>();

    // Constructores
    public Libro() {}

    public Libro(String titulo, Editorial editorial) {
        this.titulo = titulo;
        this.editorial = editorial;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public Editorial getEditorial() { return editorial; }
    public void setEditorial(Editorial editorial) { this.editorial = editorial; }

    public Set<Autor> getAutores() { return autores; }
    public void setAutores(Set<Autor> autores) { this.autores = autores; }
}