package com.biblioteca.api.models;

import jakarta.persistence.*;

@Entity
@Table(name = "libros")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String titulo;

    // Relación Muchos a 1: Muchos libros pertenecen a una sola Editorial
    @ManyToOne
    @JoinColumn(name = "editorial_id")
    private Editorial editorial;

    // Constructores, Getters y Setters
    public Libro() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public Editorial getEditorial() { return editorial; }
    public void setEditorial(Editorial editorial) { this.editorial = editorial; }
}