// js/informe_nodos.js

const API_ESTADO_NODOS = "https://nagufor.upv.edu.es/estadoNodos";

document.addEventListener("DOMContentLoaded", () => {
    const filtroTipo       = document.getElementById("filtroTipo");       // todos / inactivo / error
    const ordenarPor       = document.getElementById("ordenarPor");       // ultima_medida / tiempo_problema / id_placa
    const selectLongitud   = document.getElementById("selectLongitud");   // 5 / 15 / 20 / 100
    const btnActualizar    = document.getElementById("btnActualizarInforme");
    const tbody            = document.getElementById("tbodyNodos");

    const resumenTotal     = document.getElementById("resumenTotal");
    const resumenInactivos = document.getElementById("resumenInactivos");
    const resumenErrores   = document.getElementById("resumenErrores");

    let nodosData = []; // datos crudos venidos de la API

    // --- Eventos ---
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

    // Cargar la primera vez al entrar
    cargarInforme();

    // ---------------------------
    // 1) Llamada a la API
    // ---------------------------
    function cargarInforme() {
        const limit = selectLongitud ? selectLongitud.value : 15;
        const url   = `${API_ESTADO_NODOS}?limit=${encodeURIComponent(limit)}`;

        // Opcional: podrías mostrar algún "cargando..." aquí
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

    // ---------------------------
    // 2) Resúmenes (tarjetas)
    // ---------------------------
    function actualizarResumenes() {
        const total      = nodosData.length;
        const inactivos  = nodosData.filter(n => n.estado === "inactivo").length;
        const errores    = nodosData.filter(n => n.estado === "error").length;

        if (resumenTotal)     resumenTotal.textContent     = total;
        if (resumenInactivos) resumenInactivos.textContent = inactivos;
        if (resumenErrores)   resumenErrores.textContent   = errores;
    }

    // ---------------------------
    // 3) Aplicar filtros + orden
    // ---------------------------
    function actualizarVista() {
        if (!tbody) return;

        let lista = Array.isArray(nodosData) ? [...nodosData] : [];

        const tipoSeleccionado  = filtroTipo ? filtroTipo.value : "todos";
        const criterioOrden     = ordenarPor ? ordenarPor.value : "ultima_medida";

        // Filtro por tipo (estado)
        if (tipoSeleccionado === "inactivo") {
            lista = lista.filter(n => n.estado === "inactivo");
        } else if (tipoSeleccionado === "error") {
            lista = lista.filter(n => n.estado === "error");
        }

        // Ordenación
        lista.sort((a, b) => {
            if (criterioOrden === "id_placa") {
                // Orden alfabético por id_placa
                return (a.id_placa || "").localeCompare(b.id_placa || "");
            }

            if (criterioOrden === "tiempo_problema") {
                // Queremos primero los que más problema tienen (descendente)
                const va = a.tiempo_problema_min != null ? a.tiempo_problema_min : -1;
                const vb = b.tiempo_problema_min != null ? b.tiempo_problema_min : -1;
                return vb - va;
            }

            // Por defecto: ultima_medida (los más recientes arriba)
            const da = a.ultima_medida ? new Date(a.ultima_medida) : new Date(0);
            const db = b.ultima_medida ? new Date(b.ultima_medida) : new Date(0);
            return db - da;
        });

        // Pintar tabla
        tbody.innerHTML = "";
        lista.forEach(n => {
            const tr = document.createElement("tr");

            // Id nodo
            const tdId = document.createElement("td");
            tdId.textContent = n.id_placa || "-";
            tr.appendChild(tdId);

            // Ubicación (por ahora no viene de la API → dejamos "-")
            const tdUbic = document.createElement("td");
            const ubicacion = n.ubicacion || "-";
            tdUbic.textContent = ubicacion;
            tr.appendChild(tdUbic);

            // Última medida
            const tdUltima = document.createElement("td");
            tdUltima.textContent = n.ultima_medida
                ? formatearFechaHora(n.ultima_medida)
                : "Sin datos";
            tr.appendChild(tdUltima);

            // Estado
            const tdEstado = document.createElement("td");
            const { textoEstado, claseEstado } = obtenerEtiquetaEstado(n.estado);
            tdEstado.innerHTML = `<span class="badge-estado ${claseEstado}">${textoEstado}</span>`;
            tr.appendChild(tdEstado);

            // Tiempo con problema
            const tdTiempo = document.createElement("td");
            tdTiempo.textContent = n.tiempo_problema_min != null
                ? formatearMinutos(n.tiempo_problema_min)
                : "–";
            tr.appendChild(tdTiempo);

            tbody.appendChild(tr);
        });
    }

    // ---------------------------
    // 4) Helpers
    // ---------------------------

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

    function formatearMinutos(min) {
        if (min == null) return "–";
        const minutos = Number(min);
        if (isNaN(minutos) || minutos < 0) return "–";

        if (minutos < 60) {
            return `${minutos} min`;
        }
        const horas  = Math.floor(minutos / 60);
        const resto  = minutos % 60;
        if (resto === 0) {
            return `${horas} h`;
        }
        return `${horas} h ${resto} min`;
    }

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
