// Sesión: el token JWT se guarda en el navegador para no pedir login en cada recarga
const Sesion = {
    CLAVE: "bookspace-sesion",

    guardar(datos) {
        try { localStorage.setItem(this.CLAVE, JSON.stringify(datos)); } catch (e) { /* sin almacenamiento */ }
        this._datos = datos;
    },
    obtener() {
        if (this._datos) return this._datos;
        try { this._datos = JSON.parse(localStorage.getItem(this.CLAVE)); } catch (e) { this._datos = null; }
        return this._datos;
    },
    cerrar() {
        try { localStorage.removeItem(this.CLAVE); } catch (e) { /* sin almacenamiento */ }
        this._datos = null;
    }
};

const API = {
    // Se llama cuando la API responde 401 (token vencido o inválido)
    alExpirarSesion: null,

    async request(path, options = {}) {
        const controller = new AbortController();
        const timer = setTimeout(() => controller.abort(), CONFIG.TIMEOUT);

        const headers = { "Content-Type": "application/json", ...(options.headers || {}) };
        const sesion = Sesion.obtener();
        if (sesion?.token) headers.Authorization = "Bearer " + sesion.token;

        try {
            const res = await fetch(CONFIG.API_URL + path, { ...options, headers, signal: controller.signal });
            const texto = await res.text();
            let cuerpo = null;
            try { cuerpo = texto ? JSON.parse(texto) : null; } catch (e) { cuerpo = texto; }

            if (!res.ok) {
                if (res.status === 401 && !path.startsWith("/api/auth/") && this.alExpirarSesion) {
                    this.alExpirarSesion();
                }
                const mensaje = (cuerpo && cuerpo.error) || (typeof cuerpo === "string" && cuerpo) || "Error " + res.status;
                const error = new Error(mensaje);
                error.status = res.status;
                throw error;
            }
            return cuerpo;
        } catch (error) {
            if (error.name === "AbortError") throw new Error("La API tardó demasiado en responder");
            throw error;
        } finally {
            clearTimeout(timer);
        }
    },

    enviar(path, metodo, datos) {
        return this.request(path, { method: metodo, body: datos === undefined ? undefined : JSON.stringify(datos) });
    },

    // AUTENTICACIÓN -> /api/auth
    login(correo, password) { return this.enviar("/api/auth/login", "POST", { correo, password }); },
    registro(nombre, correo, password) { return this.enviar("/api/auth/registro", "POST", { nombre, correo, password }); },

    // LIBROS -> /api/libros  (cuerpo: { titulo, editorialId, autorIds })
    getLibros() { return this.request("/api/libros"); },
    crearLibro(libro) { return this.enviar("/api/libros", "POST", libro); },
    actualizarLibro(id, libro) { return this.enviar("/api/libros/" + id, "PUT", libro); },
    eliminarLibro(id) { return this.enviar("/api/libros/" + id, "DELETE"); },

    // SOCIOS -> /api/socios
    getSocios() { return this.request("/api/socios"); },
    crearSocio(socio) { return this.enviar("/api/socios", "POST", socio); },
    actualizarSocio(id, socio) { return this.enviar("/api/socios/" + id, "PUT", socio); },
    eliminarSocio(id) { return this.enviar("/api/socios/" + id, "DELETE"); },

    // EDITORIALES -> /api/editoriales
    getEditoriales() { return this.request("/api/editoriales"); },
    crearEditorial(editorial) { return this.enviar("/api/editoriales", "POST", editorial); },
    actualizarEditorial(id, editorial) { return this.enviar("/api/editoriales/" + id, "PUT", editorial); },
    eliminarEditorial(id) { return this.enviar("/api/editoriales/" + id, "DELETE"); },

    // AUTORES -> /api/autores
    getAutores() { return this.request("/api/autores"); },
    crearAutor(autor) { return this.enviar("/api/autores", "POST", autor); },
    actualizarAutor(id, autor) { return this.enviar("/api/autores/" + id, "PUT", autor); },
    eliminarAutor(id) { return this.enviar("/api/autores/" + id, "DELETE"); },

    // PRÉSTAMOS -> /api/prestamos  (cuerpo: { libroId, socioId, fechaLimite })
    getPrestamos() { return this.request("/api/prestamos"); },
    crearPrestamo(prestamo) { return this.enviar("/api/prestamos", "POST", prestamo); },
    devolverPrestamo(id) { return this.enviar("/api/prestamos/" + id + "/devolver", "PUT"); },
    eliminarPrestamo(id) { return this.enviar("/api/prestamos/" + id, "DELETE"); }
};
