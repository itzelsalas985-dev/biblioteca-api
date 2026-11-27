package com.biblioteca.api.models;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "prestamos")
public class Prestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fechaPrestamo;

    // Fecha en la que el socio debe regresar el libro.
    private LocalDate fechaLimite;

    // Fecha real de devolución; null mientras el préstamo está activo.
    private LocalDate fechaDevolucion;

    // Relación N a 1: muchos préstamos pertenecen a un libro (llave foránea libro_id).
    @ManyToOne
    @JoinColumn(name = "libro_id")
    private Libro libro;

    // Relación N a 1: muchos préstamos pertenecen a un socio (llave foránea socio_id).
    @ManyToOne
    @JoinColumn(name = "socio_id")
    private Socio socio;

    // Constructor vacío obligatorio para JPA
    public Prestamo() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public void setFechaPrestamo(LocalDate fechaPrestamo) { this.fechaPrestamo = fechaPrestamo; }

    public LocalDate getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(LocalDate fechaLimite) { this.fechaLimite = fechaLimite; }

    public LocalDate getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(LocalDate fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }

    public Libro getLibro() { return libro; }
    public void setLibro(Libro libro) { this.libro = libro; }

    public Socio getSocio() { return socio; }
    public void setSocio(Socio socio) { this.socio = socio; }
}
