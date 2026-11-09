const API = {
    async request(path, options = {}) {
        const controller = new AbortController();
        const timer = setTimeout(() => controller.abort(), CONFIG.TIMEOUT);
        try {
            const res = await fetch(CONFIG.API_URL + path, {
                headers: { "Content-Type": "application/json" },
                signal: controller.signal,
                ...options
            });
            if (!res.ok) {
                const detalle = await res.text();
                throw new Error(detalle || "Error " + res.status);
            }
            const texto = await res.text();
            return texto ? JSON.parse(texto) : null;
        } finally {
            clearTimeout(timer);
        }
    },

    // LIBROS  -> /libros
    getLibros() {
        return this.request("/libros");
    },
    crearLibro(libro) {
        return this.request("/libros", {
            method: "POST",
            body: JSON.stringify(libro)
        });
    },

    // SOCIOS  -> /api/socios
    getSocios() {
        return this.request("/api/socios");
    },
    crearSocio(socio) {
        return this.request("/api/socios", {
            method: "POST",
            body: JSON.stringify(socio)
        });
    },
    actualizarSocio(id, socio) {
        return this.request("/api/socios/" + id, {
            method: "PUT",
            body: JSON.stringify(socio)
        });
    },
    eliminarSocio(id) {
        return this.request("/api/socios/" + id, { method: "DELETE" });
    },

    // EDITORIALES  -> /api/editoriales
    getEditoriales() {
        return this.request("/api/editoriales");
    },
    crearEditorial(editorial) {
        return this.request("/api/editoriales", {
            method: "POST",
            body: JSON.stringify(editorial)
        });
    },

    // PRÉSTAMOS  -> /api/prestamos
    getPrestamos() {
        return this.request("/api/prestamos");
    },
    crearPrestamo(prestamo) {
        return this.request("/api/prestamos", {
            method: "POST",
            body: JSON.stringify(prestamo)
        });
    }
};