const $ = (sel) => document.querySelector(sel);
const $$ = (sel) => document.querySelectorAll(sel);

const TITULOS = {
    dashboard: "Inicio",
    libros: "Libros",
    usuarios: "Socios",
    prestamos: "Préstamos"
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

let libros = [];
let socios = [];
let prestamos = [];

/* ---------- Renderizado ---------- */

function renderLibros() {
    const tbody = $("#tabla-libros");
    if (libros.length === 0) {
        tbody.innerHTML = filaVacia(3, "Aún no hay libros registrados.");
        return;
    }
    tbody.innerHTML = libros.map((l) => `
        <tr>
            <td>${escapeHtml(l.id)}</td>
            <td>${escapeHtml(l.titulo)}</td>
            <td>${escapeHtml(l.editorial?.nombre ?? "—")}</td>
        </tr>
    `).join("");
}

function renderSocios() {
    const tbody = $("#tabla-usuarios");
    if (socios.length === 0) {
        tbody.innerHTML = filaVacia(4, "Aún no hay socios registrados.");
        return;
    }
    tbody.innerHTML = socios.map((s) => `
        <tr>
            <td>${escapeHtml(s.id)}</td>
            <td>${escapeHtml(s.nombre)}</td>
            <td>${escapeHtml(s.correo)}</td>
            <td>
                <button class="btn-edit" data-editar="${escapeHtml(s.id)}">Editar</button>
                <button class="btn-delete" data-eliminar="${escapeHtml(s.id)}">Eliminar</button>
            </td>
        </tr>
    `).join("");
}

function renderPrestamos() {
    const tbody = $("#tabla-prestamos");
    if (prestamos.length === 0) {
        tbody.innerHTML = filaVacia(5, "Aún no hay préstamos registrados.");
        return;
    }
    tbody.innerHTML = prestamos.map((p) => `
        <tr>
            <td>${escapeHtml(p.id)}</td>
            <td>${escapeHtml(p.libro?.titulo ?? "—")}</td>
            <td>${escapeHtml(p.fechaPrestamo ?? "—")}</td>
            <td>${escapeHtml(p.fechaDevolucion ?? "—")}</td>
            <td>${p.fechaDevolucion ? "✅ Devuelto" : "🟠 Activo"}</td>
        </tr>
    `).join("");
}

function renderEstadisticas() {
    const activos = prestamos.filter((p) => !p.fechaDevolucion);
    const librosOcupados = new Set(activos.map((p) => p.libro?.id)).size;

    $("#stat-libros").textContent = libros.length;
    $("#stat-usuarios").textContent = socios.length;
    $("#stat-prestamos").textContent = activos.length;
    $("#stat-disponibles").textContent = Math.max(libros.length - librosOcupados, 0);
}

function renderEstadoApi(conectado) {
    $("#api-indicator").classList.toggle("offline", !conectado);
    $("#api-text").textContent = conectado ? "API conectada" : "Sin conexión con la API";
    $("#api-url").textContent = CONFIG.API_URL;
}

/* ---------- Carga de datos ---------- */

async function cargarTodo() {
    const [rLibros, rSocios, rPrestamos] = await Promise.allSettled([
        API.getLibros(),
        API.getSocios(),
        API.getPrestamos()
    ]);

    if (rLibros.status === "fulfilled") libros = rLibros.value ?? [];
    if (rSocios.status === "fulfilled") socios = rSocios.value ?? [];
    if (rPrestamos.status === "fulfilled") prestamos = rPrestamos.value ?? [];

    const conectado = [rLibros, rSocios, rPrestamos].some((r) => r.status === "fulfilled");
    renderEstadoApi(conectado);

    if (!conectado) {
        const mensaje = "No se pudo conectar con la API. Revisa config.js y el puerto 8080.";
        $("#tabla-libros").innerHTML = filaVacia(3, mensaje);
        $("#tabla-usuarios").innerHTML = filaVacia(4, mensaje);
        $("#tabla-prestamos").innerHTML = filaVacia(5, mensaje);
        toast("⚠️ No hay conexión con la API");
        return;
    }

    renderLibros();
    renderSocios();
    renderPrestamos();
    renderEstadisticas();
}

/* ---------- Modal de libro ---------- */

function abrirModalLibro() {
    $("#form-libro").reset();
    $("#modal-libro").classList.add("show");
    $("#libro-titulo").focus();
}

function cerrarModalLibro() {
    $("#modal-libro").classList.remove("show");
}

async function guardarLibro(evento) {
    evento.preventDefault();
    const titulo = $("#libro-titulo").value.trim();
    if (!titulo) return;

    try {
        await API.crearLibro({ titulo });
        cerrarModalLibro();
        toast("📚 Libro guardado");
        libros = await API.getLibros();
        renderLibros();
        renderEstadisticas();
    } catch (error) {
        toast("❌ No se pudo guardar: " + error.message);
    }
}

/* ---------- Modal de socio ---------- */

function abrirModalSocio(socio = null) {
    $("#form-socio").reset();
    $("#socio-id").value = socio ? socio.id : "";
    $("#modal-socio-titulo").textContent = socio ? "Editar socio" : "Nuevo socio";
    if (socio) {
        $("#socio-nombre").value = socio.nombre ?? "";
        $("#socio-correo").value = socio.correo ?? "";
    }
    $("#modal-socio").classList.add("show");
    $("#socio-nombre").focus();
}

function cerrarModalSocio() {
    $("#modal-socio").classList.remove("show");
}

async function guardarSocio(evento) {
    evento.preventDefault();
    const id = $("#socio-id").value;
    const datos = {
        nombre: $("#socio-nombre").value.trim(),
        correo: $("#socio-correo").value.trim()
    };
    if (!datos.nombre || !datos.correo) return;

    try {
        if (id) {
            await API.actualizarSocio(id, datos);
            toast("✏️ Socio actualizado");
        } else {
            await API.crearSocio(datos);
            toast("👥 Socio guardado");
        }
        cerrarModalSocio();
        socios = await API.getSocios();
        renderSocios();
        renderEstadisticas();
    } catch (error) {
        toast("❌ No se pudo guardar: " + error.message);
    }
}

async function eliminarSocio(id) {
    if (!confirm("¿Seguro que quieres eliminar este socio?")) return;
    try {
        await API.eliminarSocio(id);
        toast("🗑️ Socio eliminado");
        socios = await API.getSocios();
        renderSocios();
        renderEstadisticas();
    } catch (error) {
        toast("❌ No se pudo eliminar: " + error.message);
    }
}

/* ---------- Modal de préstamo ---------- */

function abrirModalPrestamo() {
    if (libros.length === 0) {
        toast("Primero registra un libro");
        return;
    }
    $("#prestamo-libro").innerHTML = libros
        .map((l) => `<option value="${escapeHtml(l.id)}">${escapeHtml(l.titulo)}</option>`)
        .join("");
    $("#modal-prestamo").classList.add("show");
}

function cerrarModalPrestamo() {
    $("#modal-prestamo").classList.remove("show");
}

async function guardarPrestamo(evento) {
    evento.preventDefault();
    const libroId = Number($("#prestamo-libro").value);
    const hoy = new Date().toISOString().slice(0, 10);

    try {
        await API.crearPrestamo({ libro: { id: libroId }, fechaPrestamo: hoy });
        cerrarModalPrestamo();
        toast("🔄 Préstamo registrado");
        prestamos = await API.getPrestamos();
        renderPrestamos();
        renderEstadisticas();
    } catch (error) {
        toast("❌ " + error.message);
    }
}

/* ---------- Eventos ---------- */

function iniciarEventos() {
    // Navegación
    $$(".nav-item").forEach((boton) => {
        boton.addEventListener("click", () => mostrarSeccion(boton.dataset.section));
    });

    $$("[data-goto]").forEach((boton) => {
        boton.addEventListener("click", () => mostrarSeccion(boton.dataset.goto));
    });

    // Libros
    $("#btn-nuevo-libro").addEventListener("click", abrirModalLibro);
    $("#quick-add-libro").addEventListener("click", () => {
        mostrarSeccion("libros");
        abrirModalLibro();
    });

    $("#cerrar-modal").addEventListener("click", cerrarModalLibro);
    $("#cancelar-modal").addEventListener("click", cerrarModalLibro);
    $("#modal-libro").addEventListener("click", (e) => {
        if (e.target.id === "modal-libro") cerrarModalLibro();
    });

    $("#form-libro").addEventListener("submit", guardarLibro);

    // Socios
    $("#btn-nuevo-socio").addEventListener("click", () => abrirModalSocio());
    $("#cerrar-modal-socio").addEventListener("click", cerrarModalSocio);
    $("#cancelar-modal-socio").addEventListener("click", cerrarModalSocio);
    $("#modal-socio").addEventListener("click", (e) => {
        if (e.target.id === "modal-socio") cerrarModalSocio();
    });
    $("#form-socio").addEventListener("submit", guardarSocio);

    $("#tabla-usuarios").addEventListener("click", (e) => {
        const btnEditar = e.target.closest("[data-editar]");
        const btnEliminar = e.target.closest("[data-eliminar]");
        if (btnEditar) {
            const socio = socios.find((s) => String(s.id) === btnEditar.dataset.editar);
            if (socio) abrirModalSocio(socio);
        }
        if (btnEliminar) eliminarSocio(btnEliminar.dataset.eliminar);
    });

    // Préstamos
    $("#btn-nuevo-prestamo").addEventListener("click", abrirModalPrestamo);
    $("#cerrar-modal-prestamo").addEventListener("click", cerrarModalPrestamo);
    $("#cancelar-modal-prestamo").addEventListener("click", cerrarModalPrestamo);
    $("#modal-prestamo").addEventListener("click", (e) => {
        if (e.target.id === "modal-prestamo") cerrarModalPrestamo();
    });
    $("#form-prestamo").addEventListener("submit", guardarPrestamo);
}

/* ---------- Inicio ---------- */

document.addEventListener("DOMContentLoaded", () => {
    iniciarEventos();
    cargarTodo();
});