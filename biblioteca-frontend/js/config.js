// Detecta la URL de la API automáticamente:
// - En Codespaces el front corre en el puerto 5500 y la API en el 8080 del mismo Codespace.
// - En local se usa http://localhost:8080.
function detectarApiUrl() {
    const host = window.location.hostname;
    if (host.endsWith(".app.github.dev")) {
        return "https://" + host.replace(/-\d+\.app\.github\.dev$/, "-8080.app.github.dev");
    }
    return "http://localhost:8080";
}

const CONFIG = {
    // URL de la API de Spring Boot (sin "/" al final)
    API_URL: detectarApiUrl(),

    APP_NAME: "BookSpace",

    // Tiempo máximo de espera (milisegundos)
    TIMEOUT: 10000
};
