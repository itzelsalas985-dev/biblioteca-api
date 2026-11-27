package com.biblioteca.api.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "libros")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    // Relación N a 1: muchos libros pertenecen a una sola editorial (llave foránea editorial_id).
    @ManyToOne
    @JoinColumn(name = "editorial_id")
    private Editorial editorial;

    // Relación N a M: un libro puede tener varios autores y un autor varios libros.
    // Hibernate crea la tabla intermedia libro_autor (libro_id, autor_id).
    @ManyToMany
    @JoinTable(
        name = "libro_autor",
        joinColumns = @JoinColumn(name = "libro_id"),
        inverseJoinColumns = @JoinColumn(name = "autor_id")
    )
    private Set<Autor> autores = new HashSet<>();

    // Relación 1 a N: un libro tiene muchos préstamos a lo largo del tiempo.
    // Si se borra el libro, se borra su historial de préstamos.
    @OneToMany(mappedBy = "libro", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private List<Prestamo> prestamos = new ArrayList<>();

    public Libro() {}

    public Libro(String titulo, Editorial editorial) {
        this.titulo = titulo;
        this.editorial = editorial;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public Editorial getEditorial() { return editorial; }
    public void setEditorial(Editorial editorial) { this.editorial = editorial; }

    public Set<Autor> getAutores() { return autores; }
    public void setAutores(Set<Autor> autores) { this.autores = autores; }

    public List<Prestamo> getPrestamos() { return prestamos; }
    public void setPrestamos(List<Prestamo> prestamos) { this.prestamos = prestamos; }
}
