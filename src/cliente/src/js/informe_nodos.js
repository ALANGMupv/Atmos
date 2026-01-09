/**
 * @file informe_nodos.js
 * @brief Generación del informe de estado de nodos.
 *
 * Este script se encarga de:
 * - Consultar el estado de los nodos desde la API.
 * - Mostrar resúmenes (total, inactivos, errores).
 * - Aplicar filtros por tipo de estado.
 * - Ordenar los resultados según distintos criterios.
 * - Renderizar la tabla del informe de nodos.
 */

const API_ESTADO_NODOS = "https://nagufor.upv.edu.es/estadoNodos";

/**
 * @brief Inicialización del informe al cargar el DOM.
 */
document.addEventListener("DOMContentLoaded", () => {

    /** Selector de filtro por tipo de nodo */
    const filtroTipo = document.getElementById("filtroTipo");

    /** Selector de criterio de ordenación */
    const ordenarPor = document.getElementById("ordenarPor");

    /** Selector del número máximo de nodos a cargar */
    const selectLongitud = document.getElementById("selectLongitud");

    /** Botón para actualizar manualmente el informe */
    const btnActualizar = document.getElementById("btnActualizarInforme");

    /** <tbody> de la tabla de nodos */
    const tbody = document.getElementById("tbodyNodos");

    /** Contadores de resumen */
    const resumenTotal = document.getElementById("resumenTotal");
    const resumenInactivos = document.getElementById("resumenInactivos");
    const resumenErrores = document.getElementById("resumenErrores");

    /** Datos crudos de nodos recibidos desde la API */
    let nodosData = [];

    // ======================================================
    // EVENTOS
    // ======================================================

    if (btnActualizar) {
        btnActualizar.addEventListener("click", cargarInforme);
    }
    if (selectLongitud) {
        selectLongitud.addEventListener("change", cargarInforme);
    }
    if (filtroTipo) {
        filtroTipo.addEventListener("change", actualizarVista);
    }
    if (ordenarPor) {
        ordenarPor.addEventListener("change", actualizarVista);
    }

    // Carga inicial al entrar en la página
    cargarInforme();

    // ======================================================
    // 1) LLAMADA A LA API
    // ======================================================

    /**
     * @brief Carga el informe de nodos desde la API.
     */
    function cargarInforme() {
        const limit = selectLongitud ? selectLongitud.value : 15;
        const url = `${API_ESTADO_NODOS}?limit=${encodeURIComponent(limit)}`;

        fetch(url)
            .then(resp => resp.json())
            .then(data => {
                if (!data || data.status !== "ok" || !Array.isArray(data.nodos)) {
                    console.error("Respuesta inesperada en /estadoNodos:", data);
                    nodosData = [];
                } else {
                    nodosData = data.nodos;
                }

                actualizarResumenes();
                actualizarVista();
            })
            .catch(err => {
                console.error("Error al llamar a /estadoNodos:", err);
                nodosData = [];
                actualizarResumenes();
                actualizarVista();
            });
    }

    // ======================================================
    // 2) RESÚMENES (TARJETAS)
    // ======================================================

    /**
     * @brief Actualiza los contadores de resumen de nodos.
     */
    function actualizarResumenes() {
        const total = nodosData.length;
        const inactivos = nodosData.filter(n => n.estado === "inactivo").length;
        const errores = nodosData.filter(n => n.estado === "error").length;

        if (resumenTotal) resumenTotal.textContent = total;
        if (resumenInactivos) resumenInactivos.textContent = inactivos;
        if (resumenErrores) resumenErrores.textContent = errores;
    }

    // ======================================================
    // 3) FILTROS Y ORDENACIÓN
    // ======================================================

    /**
     * @brief Aplica filtros y ordenación y renderiza la tabla.
     */
    function actualizarVista() {
        if (!tbody) return;

        let lista = Array.isArray(nodosData) ? [...nodosData] : [];

        const tipoSeleccionado = filtroTipo ? filtroTipo.value : "todos";
        const criterioOrden = ordenarPor ? ordenarPor.value : "ultima_medida";

        if (tipoSeleccionado === "inactivo") {
            lista = lista.filter(n => n.estado === "inactivo");
        } else if (tipoSeleccionado === "error") {
            lista = lista.filter(n => n.estado === "error");
        }

        lista.sort((a, b) => {

            if (criterioOrden === "id_placa") {
                return (a.id_placa || "").localeCompare(b.id_placa || "");
            }

            if (criterioOrden === "tiempo_problema") {
                const va = a.tiempo_problema_min != null ? a.tiempo_problema_min : -1;
                const vb = b.tiempo_problema_min != null ? b.tiempo_problema_min : -1;
                return vb - va;
            }

            const da = a.ultima_medida ? new Date(a.ultima_medida) : new Date(0);
            const db = b.ultima_medida ? new Date(b.ultima_medida) : new Date(0);
            return db - da;
        });

        tbody.innerHTML = "";
        lista.forEach(n => {
            const tr = document.createElement("tr");

            const tdId = document.createElement("td");
            tdId.textContent = n.id_placa || "-";
            tr.appendChild(tdId);

            const tdUbic = document.createElement("td");
            tdUbic.textContent = n.ubicacion || "-";
            tr.appendChild(tdUbic);

            const tdUltima = document.createElement("td");
            tdUltima.textContent = n.ultima_medida
                ? formatearFechaHora(n.ultima_medida)
                : "Sin datos";
            tr.appendChild(tdUltima);

            const tdEstado = document.createElement("td");
            const { textoEstado, claseEstado } = obtenerEtiquetaEstado(n.estado);
            tdEstado.innerHTML =
                `<span class="badge-estado ${claseEstado}">${textoEstado}</span>`;
            tr.appendChild(tdEstado);

            const tdTiempo = document.createElement("td");
            tdTiempo.textContent = n.tiempo_problema_min != null
                ? formatearMinutos(n.tiempo_problema_min)
                : "–";
            tr.appendChild(tdTiempo);

            tbody.appendChild(tr);
        });
    }

    // ======================================================
    // 4) FUNCIONES AUXILIARES
    // ======================================================

    /**
     * @brief Formatea una fecha y hora en formato legible.
     *
     * @param {string} valor Fecha en formato ISO o compatible.
     * @return {string} Fecha y hora formateadas.
     */
    function formatearFechaHora(valor) {
        try {
            const d = new Date(valor);
            if (isNaN(d.getTime())) return valor;
            return d.toLocaleString("es-ES", {
                year: "numeric",
                month: "2-digit",
                day: "2-digit",
                hour: "2-digit",
                minute: "2-digit"
            });
        } catch {
            return valor;
        }
    }

    /**
     * @brief Convierte minutos a un formato legible (h/min).
     *
     * @param {number} min Minutos totales.
     * @return {string} Tiempo formateado.
     */
    function formatearMinutos(min) {
        if (min == null) return "–";
        const minutos = Number(min);
        if (isNaN(minutos) || minutos < 0) return "–";

        if (minutos < 60) {
            return `${minutos} min`;
        }
        const horas = Math.floor(minutos / 60);
        const resto = minutos % 60;
        if (resto === 0) {
            return `${horas} h`;
        }
        return `${horas} h ${resto} min`;
    }

    /**
     * @brief Obtiene la etiqueta visual del estado del nodo.
     *
     * @param {string} estado Estado del nodo.
     * @return {{textoEstado: string, claseEstado: string}} Etiqueta y clase CSS.
     */
    function obtenerEtiquetaEstado(estado) {
        let texto = "Activo";
        let clase = "badge-activo";

        if (estado === "inactivo") {
            texto = "Inactivo";
            clase = "badge-inactivo";
        } else if (estado === "error") {
            texto = "Lecturas erróneas";
            clase = "badge-error";
        }

        return { textoEstado: texto, claseEstado: clase };
    }
});
