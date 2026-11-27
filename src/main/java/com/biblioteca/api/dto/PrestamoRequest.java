package com.biblioteca.api.dto;

import java.time.LocalDate;

// Datos para registrar un préstamo. Si no se manda fechaLimite se dan 14 días.
public record PrestamoRequest(Long libroId, Long socioId, LocalDate fechaLimite) {
}
