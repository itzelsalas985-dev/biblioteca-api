const $ = (sel) => document.querySelector(sel);
const $$ = (sel) => document.querySelectorAll(sel);

const TITULOS = {
    dashboard: "Inicio",
    libros: "Libros",
    usuarios: "Socios",
    prestamos: "Préstamos",
    editoriales: "Editoriales",
    autores: "Autores"
};

/* ---------- Utilidades ---------- */

function escapeHtml(texto) {
    return String(texto ?? "").replace(/[&<>"']/g, (c) => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    }[c]));
}

let toastTimer;
function toast(mensaje) {
    const el = $("#toast");
    el.textContent = mensaje;
    el.classList.add("show");
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => el.classList.remove("show"), 3000);
}

function filaVacia(columnas, texto, clase = "empty-message") {
    return `<tr><td colspan="${columnas}" class="${clase}">${texto}</td></tr>`;
}

function hoyISO() {
    const d = new Date();
    d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
    return d.toISOString().slice(0, 10);
}

function sumarDias(fechaISO, dias) {
    const d = new Date(fechaISO + "T12:00:00");
    d.setDate(d.getDate() + dias);
    return d.toISOString().slice(0, 10);
}

// "2026-11-27" -> "27/11/2026"
function formatoFecha(fechaISO) {
    if (!fechaISO) return "—";
    const [a, m, d] = fechaISO.split("-");
    return `${d}/${m}/${a}`;
}

function iniciales(nombre) {
    return String(nombre || "?").trim().split(/\s+/).slice(0, 2).map((p) => p[0]).join("").toUpperCase();
}

function botones(tipo, id) {
    return `<div class="acciones">
        <button class="btn-edit" data-accion="editar" data-tipo="${tipo}" data-id="${escapeHtml(id)}">Editar</button>
        <button class="btn-delete" data-accion="eliminar" data-tipo="${tipo}" data-id="${escapeHtml(id)}">Eliminar</button>
    </div>`;
}

function abrirModal(id) { $("#" + id).classList.add("show"); }
function cerrarModal(id) { $("#" + id).classList.remove("show"); }

/* ---------- Navegación ---------- */

function mostrarSeccion(id) {
    $$(".page-section").forEach((s) => {
        s.style.display = s.id === id ? "block" : "none";
    });
    $$(".nav-item").forEach((b) => {
        b.classList.toggle("active", b.dataset.section === id);
    });
    $("#page-title").textContent = TITULOS[id];
    $("#breadcrumb").textContent = "BookSpace / " + TITULOS[id];
}

/* ---------- Datos en memoria ---------- */

const datos = {
    libros: [],
    socios: [],
    prestamos: [],
    editoriales: [],
    autores: []
};

const prestamosActivos = () => datos.prestamos.filter((p) => !p.fechaDevolucion);
const libroPrestado = (libroId) => prestamosActivos().some((p) => p.libro?.id === libroId);

/* ---------- Renderizado ---------- */

function renderLibros() {
    const tbody = $("#tabla-libros");
    if (datos.libros.length === 0) {
        tbody.innerHTML = filaVacia(6, "Aún no hay libros registrados.");
        return;
    }
    tbody.innerHTML = datos.libros.map((l) => {
        const autores = (l.autores || []).map((a) => a.nombre).join(", ") || "—";
        const estado = libroPrestado(l.id)
            ? `<span class="badge naranja">Prestado</span>`
            : `<span class="badge verde">Disponible</span>`;
        return `
        <tr>
            <td>${escapeHtml(l.id)}</td>
            <td>${escapeHtml(l.titulo)}</td>
            <td>${escapeHtml(l.editorial?.nombre ?? "—")}</td>
            <td>${escapeHtml(autores)}</td>
            <td>${estado}</td>
            <td>${botones("libro", l.id)}</td>
        </tr>`;
    }).join("");
}

function renderSocios() {
    const tbody = $("#tabla-usuarios");
    if (datos.socios.length === 0) {
        tbody.innerHTML = filaVacia(5, "Aún no hay socios registrados.");
        return;
    }
    tbody.innerHTML = datos.socios.map((s) => {
        const activos = prestamosActivos().filter((p) => p.socio?.id === s.id).length;
        return `
        <tr>
            <td>${escapeHtml(s.id)}</td>
            <td>${escapeHtml(s.nombre)}</td>
            <td>${escapeHtml(s.correo)}</td>
            <td>${activos}</td>
            <td>${botones("socio", s.id)}</td>
        </tr>`;
    }).join("");
}

function renderPrestamos() {
    const tbody = $("#tabla-prestamos");
    if (datos.prestamos.length === 0) {
        tbody.innerHTML = filaVacia(8, "Aún no hay préstamos registrados.");
        return;
    }
    const hoy = hoyISO();

    tbody.innerHTML = datos.prestamos.map((p) => {
        let estado = `<span class="badge naranja">Activo</span>`;
        if (p.fechaDevolucion) {
            estado = `<span class="badge verde">Devuelto</span>`;
        } else if (p.fechaLimite && p.fechaLimite < hoy) {
            estado = `<span class="badge rojo">Vencido</span>`;
        }

        const btnDevolver = p.fechaDevolucion
            ? ""
            : `<button class="btn-edit" data-accion="devolver" data-tipo="prestamo" data-id="${escapeHtml(p.id)}">Devolver</button>`;

        return `
        <tr>
            <td>${escapeHtml(p.id)}</td>
            <td>${escapeHtml(p.libro?.titulo ?? "—")}</td>
            <td>${escapeHtml(p.socio?.nombre ?? "—")}</td>
            <td>${formatoFecha(p.fechaPrestamo)}</td>
            <td>${formatoFecha(p.fechaLimite)}</td>
            <td>${formatoFecha(p.fechaDevolucion)}</td>
            <td>${estado}</td>
            <td><div class="acciones">
                ${btnDevolver}
                <button class="btn-delete" data-accion="eliminar" data-tipo="prestamo" data-id="${escapeHtml(p.id)}">Eliminar</button>
            </div></td>
        </tr>`;
    }).join("");
}

function renderEditoriales() {
    const tbody = $("#tabla-editoriales");
    if (datos.editoriales.length === 0) {
        tbody.innerHTML = filaVacia(5, "Aún no hay editoriales registradas.");
        return;
    }
    tbody.innerHTML = datos.editoriales.map((e) => {
        const total = datos.libros.filter((l) => l.editorial?.id === e.id).length;
        return `
        <tr>
            <td>${escapeHtml(e.id)}</td>
            <td>${escapeHtml(e.nombre)}</td>
            <td>${escapeHtml(e.pais || "—")}</td>
            <td>${total}</td>
            <td>${botones("editorial", e.id)}</td>
        </tr>`;
    }).join("");
}

function renderAutores() {
    const tbody = $("#tabla-autores");
    if (datos.autores.length === 0) {
        tbody.innerHTML = filaVacia(5, "Aún no hay autores registrados.");
        return;
    }
    tbody.innerHTML = datos.autores.map((a) => {
        const total = datos.libros.filter((l) => (l.autores || []).some((x) => x.id === a.id)).length;
        return `
        <tr>
            <td>${escapeHtml(a.id)}</td>
            <td>${escapeHtml(a.nombre)}</td>
            <td>${escapeHtml(a.nacionalidad || "—")}</td>
            <td>${total}</td>
            <td>${botones("autor", a.id)}</td>
        </tr>`;
    }).join("");
}

function renderEstadisticas() {
    const ocupados = new Set(prestamosActivos().map((p) => p.libro?.id)).size;
    $("#stat-libros").textContent = datos.libros.length;
    $("#stat-usuarios").textContent = datos.socios.length;
    $("#stat-prestamos").textContent = prestamosActivos().length;
    $("#stat-disponibles").textContent = Math.max(datos.libros.length - ocupados, 0);
}

function renderTodo() {
    renderLibros();
    renderSocios();
    renderPrestamos();
    renderEditoriales();
    renderAutores();
    renderEstadisticas();
}

function renderEstadoApi(conectado) {
    $("#api-indicator").classList.toggle("offline", !conectado);
    $("#api-text").textContent = conectado ? "API conectada" : "Sin conexión con la API";
    $("#api-url").textContent = CONFIG.API_URL;
}

/* ---------- Carga de datos ---------- */

const CARGADORES = {
    libros: () => API.getLibros(),
    socios: () => API.getSocios(),
    prestamos: () => API.getPrestamos(),
    editoriales: () => API.getEditoriales(),
    autores: () => API.getAutores()
};

async function recargar(...nombres) {
    const lista = nombres.length ? nombres : Object.keys(CARGADORES);
    const resultados = await Promise.allSettled(lista.map((n) => CARGADORES[n]()));
    resultados.forEach((r, i) => {
        if (r.status === "fulfilled") datos[lista[i]] = r.value ?? [];
    });
    return resultados;
}

async function cargarTodo() {
    const resultados = await recargar();
    const conectado = resultados.some((r) => r.status === "fulfilled");
    renderEstadoApi(conectado);

    if (!conectado) {
        const mensaje = "No se pudo conectar con la API. Revisa que corra en el puerto 8080 y que el puerto sea público.";
        ["#tabla-libros", "#tabla-usuarios", "#tabla-prestamos", "#tabla-editoriales", "#tabla-autores"]
            .forEach((t) => { $(t).innerHTML = filaVacia(8, mensaje); });
        toast("⚠️ No hay conexión con la API");
        return;
    }
    renderTodo();
}

// Ejecuta una acción, recarga lo necesario y muestra el resultado
async function ejecutar(accion, mensajeOk, ...recargarDatos) {
    try {
        await accion();
        await recargar(...recargarDatos);
        renderTodo();
        toast(mensajeOk);
        return true;
    } catch (error) {
        toast("❌ " + error.message);
        return false;
    }
}

/* ---------- Libros ---------- */

function abrirModalLibro(libro = null) {
    $("#form-libro").reset();
    $("#libro-id").value = libro ? libro.id : "";
    $("#modal-libro-titulo").textContent = libro ? "Editar libro" : "Nuevo libro";
    $("#libro-titulo").value = libro?.titulo ?? "";

    $("#libro-editorial").innerHTML = `<option value="">Sin editorial</option>` + datos.editoriales
        .map((e) => `<option value="${escapeHtml(e.id)}">${escapeHtml(e.nombre)}</option>`).join("");
    $("#libro-editorial").value = libro?.editorial?.id ?? "";

    const seleccionados = new Set((libro?.autores || []).map((a) => a.id));
    $("#libro-autores").innerHTML = datos.autores.length
        ? datos.autores.map((a) => `
            <label><input type="checkbox" value="${escapeHtml(a.id)}" ${seleccionados.has(a.id) ? "checked" : ""}>
            ${escapeHtml(a.nombre)}</label>`).join("")
        : `<span class="vacio">No hay autores. Agrégalos en Catálogos › Autores.</span>`;

    abrirModal("modal-libro");
    $("#libro-titulo").focus();
}

async function guardarLibro(evento) {
    evento.preventDefault();
    const id = $("#libro-id").value;
    const libro = {
        titulo: $("#libro-titulo").value.trim(),
        editorialId: $("#libro-editorial").value ? Number($("#libro-editorial").value) : null,
        autorIds: [...$$("#libro-autores input:checked")].map((c) => Number(c.value))
    };
    if (!libro.titulo) return;

    const ok = await ejecutar(
        () => (id ? API.actualizarLibro(id, libro) : API.crearLibro(libro)),
        id ? "✏️ Libro actualizado" : "📚 Libro guardado",
        "libros"
    );
    if (ok) cerrarModal("modal-libro");
}

/* ---------- Socios ---------- */

function abrirModalSocio(socio = null) {
    $("#form-socio").reset();
    $("#socio-id").value = socio ? socio.id : "";
    $("#modal-socio-titulo").textContent = socio ? "Editar socio" : "Nuevo socio";
    $("#socio-nombre").value = socio?.nombre ?? "";
    $("#socio-correo").value = socio?.correo ?? "";
    abrirModal("modal-socio");
    $("#socio-nombre").focus();
}

async function guardarSocio(evento) {
    evento.preventDefault();
    const id = $("#socio-id").value;
    const socio = {
        nombre: $("#socio-nombre").value.trim(),
        correo: $("#socio-correo").value.trim()
    };
    if (!socio.nombre || !socio.correo) return;

    const ok = await ejecutar(
        () => (id ? API.actualizarSocio(id, socio) : API.crearSocio(socio)),
        id ? "✏️ Socio actualizado" : "👥 Socio guardado",
        "socios", "prestamos"
    );
    if (ok) cerrarModal("modal-socio");
}

/* ---------- Préstamos ---------- */

function abrirModalPrestamo() {
    const disponibles = datos.libros.filter((l) => !libroPrestado(l.id));
    if (disponibles.length === 0) {
        toast("No hay libros disponibles para prestar");
        return;
    }
    if (datos.socios.length === 0) {
        toast("Primero registra un socio");
        return;
    }
    $("#prestamo-libro").innerHTML = disponibles
        .map((l) => `<option value="${escapeHtml(l.id)}">${escapeHtml(l.titulo)}</option>`).join("");
    $("#prestamo-socio").innerHTML = datos.socios
        .map((s) => `<option value="${escapeHtml(s.id)}">${escapeHtml(s.nombre)}</option>`).join("");
    $("#prestamo-limite").min = hoyISO();
    $("#prestamo-limite").value = sumarDias(hoyISO(), 14);
    abrirModal("modal-prestamo");
}

async function guardarPrestamo(evento) {
    evento.preventDefault();
    const prestamo = {
        libroId: Number($("#prestamo-libro").value),
        socioId: Number($("#prestamo-socio").value),
        fechaLimite: $("#prestamo-limite").value || null
    };
    const ok = await ejecutar(() => API.crearPrestamo(prestamo), "🔄 Préstamo registrado", "prestamos");
    if (ok) cerrarModal("modal-prestamo");
}

/* ---------- Editoriales ---------- */

function abrirModalEditorial(editorial = null) {
    $("#form-editorial").reset();
    $("#editorial-id").value = editorial ? editorial.id : "";
    $("#modal-editorial-titulo").textContent = editorial ? "Editar editorial" : "Nueva editorial";
    $("#editorial-nombre").value = editorial?.nombre ?? "";
    $("#editorial-pais").value = editorial?.pais ?? "";
    abrirModal("modal-editorial");
    $("#editorial-nombre").focus();
}

async function guardarEditorial(evento) {
    evento.preventDefault();
    const id = $("#editorial-id").value;
    const editorial = {
        nombre: $("#editorial-nombre").value.trim(),
        pais: $("#editorial-pais").value.trim()
    };
    if (!editorial.nombre) return;

    const ok = await ejecutar(
        () => (id ? API.actualizarEditorial(id, editorial) : API.crearEditorial(editorial)),
        id ? "✏️ Editorial actualizada" : "🏢 Editorial guardada",
        "editoriales", "libros"
    );
    if (ok) cerrarModal("modal-editorial");
}

/* ---------- Autores ---------- */

function abrirModalAutor(autor = null) {
    $("#form-autor").reset();
    $("#autor-id").value = autor ? autor.id : "";
    $("#modal-autor-titulo").textContent = autor ? "Editar autor" : "Nuevo autor";
    $("#autor-nombre").value = autor?.nombre ?? "";
    $("#autor-nacionalidad").value = autor?.nacionalidad ?? "";
    abrirModal("modal-autor");
    $("#autor-nombre").focus();
}

async function guardarAutor(evento) {
    evento.preventDefault();
    const id = $("#autor-id").value;
    const autor = {
        nombre: $("#autor-nombre").value.trim(),
        nacionalidad: $("#autor-nacionalidad").value.trim()
    };
    if (!autor.nombre) return;

    const ok = await ejecutar(
        () => (id ? API.actualizarAutor(id, autor) : API.crearAutor(autor)),
        id ? "✏️ Autor actualizado" : "✍️ Autor guardado",
        "autores", "libros"
    );
    if (ok) cerrarModal("modal-autor");
}

/* ---------- Acciones de las tablas (editar, eliminar, devolver) ---------- */

const ACCIONES = {
    libro: {
        lista: "libros",
        editar: abrirModalLibro,
        eliminar: (id) => ejecutar(() => API.eliminarLibro(id), "🗑️ Libro eliminado", "libros", "prestamos"),
        confirmar: "¿Seguro que quieres eliminar este libro? También se borra su historial de préstamos."
    },
    socio: {
        lista: "socios",
        editar: abrirModalSocio,
        eliminar: (id) => ejecutar(() => API.eliminarSocio(id), "🗑️ Socio eliminado", "socios", "prestamos"),
        confirmar: "¿Seguro que quieres eliminar este socio?"
    },
    editorial: {
        lista: "editoriales",
        editar: abrirModalEditorial,
        eliminar: (id) => ejecutar(() => API.eliminarEditorial(id), "🗑️ Editorial eliminada", "editoriales"),
        confirmar: "¿Seguro que quieres eliminar esta editorial?"
    },
    autor: {
        lista: "autores",
        editar: abrirModalAutor,
        eliminar: (id) => ejecutar(() => API.eliminarAutor(id), "🗑️ Autor eliminado", "autores", "libros"),
        confirmar: "¿Seguro que quieres eliminar este autor? Se quitará de sus libros."
    },
    prestamo: {
        lista: "prestamos",
        eliminar: (id) => ejecutar(() => API.eliminarPrestamo(id), "🗑️ Préstamo eliminado", "prestamos"),
        devolver: (id) => ejecutar(() => API.devolverPrestamo(id), "✅ Libro devuelto", "prestamos"),
        confirmar: "¿Seguro que quieres eliminar este préstamo?"
    }
};

function manejarAccionTabla(evento) {
    const boton = evento.target.closest("[data-accion]");
    if (!boton) return;
    const { accion, tipo, id } = boton.dataset;
    const config = ACCIONES[tipo];

    if (accion === "editar") {
        const registro = datos[config.lista].find((r) => String(r.id) === id);
        if (registro) config.editar(registro);
    } else if (accion === "eliminar") {
        if (confirm(config.confirmar)) config.eliminar(id);
    } else if (accion === "devolver") {
        config.devolver(id);
    }
}

/* ---------- Sesión ---------- */

function mostrarLogin() {
    $("#app").style.display = "none";
    $("#login-screen").style.display = "flex";
    $("#login-api").textContent = "API: " + CONFIG.API_URL;
    $("#login-correo").focus();
}

function mostrarApp() {
    const sesion = Sesion.obtener();
    $("#login-screen").style.display = "none";
    $("#app").style.display = "flex";
    $("#perfil-nombre").textContent = sesion.nombre;
    $("#perfil-rol").textContent = sesion.rol === "ADMIN" ? "Administrador" : "Bibliotecario";
    $("#perfil-iniciales").textContent = iniciales(sesion.nombre);
    $("#bienvenida").textContent = "Hola, " + sesion.nombre.split(" ")[0];
    mostrarSeccion("dashboard");
    cargarTodo();
}

function cambiarFormulario(registro) {
    $("#form-login").style.display = registro ? "none" : "block";
    $("#form-registro").style.display = registro ? "block" : "none";
    $("#login-error").textContent = "";
    $("#registro-error").textContent = "";
}

async function iniciarSesion(evento) {
    evento.preventDefault();
    $("#login-error").textContent = "";
    try {
        const sesion = await API.login($("#login-correo").value.trim(), $("#login-password").value);
        Sesion.guardar(sesion);
        $("#form-login").reset();
        mostrarApp();
    } catch (error) {
        $("#login-error").textContent = error.status ? error.message : "No se pudo conectar con la API.";
    }
}

async function registrarse(evento) {
    evento.preventDefault();
    $("#registro-error").textContent = "";
    try {
        const sesion = await API.registro(
            $("#registro-nombre").value.trim(),
            $("#registro-correo").value.trim(),
            $("#registro-password").value
        );
        Sesion.guardar(sesion);
        $("#form-registro").reset();
        cambiarFormulario(false);
        mostrarApp();
        toast("👋 Cuenta creada");
    } catch (error) {
        $("#registro-error").textContent = error.status ? error.message : "No se pudo conectar con la API.";
    }
}

function cerrarSesion() {
    Sesion.cerrar();
    mostrarLogin();
}

/* ---------- Eventos ---------- */

function iniciarEventos() {
    // Sesión
    $("#form-login").addEventListener("submit", iniciarSesion);
    $("#form-registro").addEventListener("submit", registrarse);
    $("#ir-registro").addEventListener("click", () => cambiarFormulario(true));
    $("#ir-login").addEventListener("click", () => cambiarFormulario(false));
    $("#btn-logout").addEventListener("click", cerrarSesion);
    API.alExpirarSesion = () => {
        Sesion.cerrar();
        mostrarLogin();
        $("#login-error").textContent = "Tu sesión expiró. Vuelve a iniciar sesión.";
    };

    // Navegación
    $$(".nav-item").forEach((boton) => {
        boton.addEventListener("click", () => mostrarSeccion(boton.dataset.section));
    });
    $$("[data-goto]").forEach((boton) => {
        boton.addEventListener("click", () => mostrarSeccion(boton.dataset.goto));
    });

    // Botones "Nuevo"
    $("#btn-nuevo-libro").addEventListener("click", () => abrirModalLibro());
    $("#btn-nuevo-socio").addEventListener("click", () => abrirModalSocio());
    $("#btn-nuevo-prestamo").addEventListener("click", abrirModalPrestamo);
    $("#btn-nueva-editorial").addEventListener("click", () => abrirModalEditorial());
    $("#btn-nuevo-autor").addEventListener("click", () => abrirModalAutor());
    $("#quick-add-libro").addEventListener("click", () => {
        mostrarSeccion("libros");
        abrirModalLibro();
    });
    $("#quick-add-prestamo").addEventListener("click", () => {
        mostrarSeccion("prestamos");
        abrirModalPrestamo();
    });

    // Formularios
    $("#form-libro").addEventListener("submit", guardarLibro);
    $("#form-socio").addEventListener("submit", guardarSocio);
    $("#form-prestamo").addEventListener("submit", guardarPrestamo);
    $("#form-editorial").addEventListener("submit", guardarEditorial);
    $("#form-autor").addEventListener("submit", guardarAutor);

    // Cerrar modales (botón × , Cancelar o clic fuera)
    $$("[data-cerrar]").forEach((boton) => {
        boton.addEventListener("click", () => cerrarModal(boton.dataset.cerrar));
    });
    $$(".modal-overlay").forEach((overlay) => {
        overlay.addEventListener("click", (e) => {
            if (e.target === overlay) cerrarModal(overlay.id);
        });
    });
    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape") $$(".modal-overlay.show").forEach((m) => cerrarModal(m.id));
    });

    // Botones dentro de las tablas
    ["#tabla-libros", "#tabla-usuarios", "#tabla-prestamos", "#tabla-editoriales", "#tabla-autores"]
        .forEach((tabla) => $(tabla).addEventListener("click", manejarAccionTabla));
}

/* ---------- Inicio ---------- */

document.addEventListener("DOMContentLoaded", () => {
    iniciarEventos();
    if (Sesion.obtener()?.token) {
        mostrarApp();
    } else {
        mostrarLogin();
    }
});
