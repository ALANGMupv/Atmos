<?php
/**
 * @file informeNodos.php
 * @brief Vista del informe de nodos con incidencias (T019).
 *
 * Esta página muestra un informe visual del estado de los nodos
 * del sistema ATMOS. La vista incluye:
 *  - Filtros por estado del nodo.
 *  - Ordenación por diferentes criterios.
 *  - Resumen numérico de nodos totales, inactivos y con errores.
 *  - Tabla detallada de nodos con incidencias.
 *
 * Importante:
 *  - Esta vista es únicamente frontend (HTML + CSS + JS).
 *  - La lógica de negocio y el endpoint `/estadoNodos`
 *    se gestionan en el backend Node.js / Express.
 *
 * Dependencias:
 *  - js/informe_nodos.js
 *  - css/informe_nodos.css
 *  - partials/headerEmpresas.php
 *
 * @author Equipo ATMOS
 * @version 1.0
 */
?>
<link rel="stylesheet" href="css/informe_nodos.css">
<script src="js/informe_nodos.js" defer></script>

<?php
/**
 * @brief Marca el apartado "nodos" como activo en el menú de empresas.
 */
$active = 'nodos';

/**
 * @brief Inclusión del header específico para empresas.
 */
include __DIR__ . '/partials/headerEmpresas.php';
?>

<div class="panel-informe-nodos">

    <!--
    /**
     * @section Cabecera del informe
     * @brief Filtros de visualización y resumen superior.
     *
     * Incluye:
     *  - Filtro por tipo de nodo (todos, inactivos, errores).
     *  - Selector de criterio de ordenación.
     *  - Botón de actualización manual del informe.
     *  - Tarjetas resumen con métricas clave.
     */
    -->
    <div class="informe-header">

        <div class="filtros-informe">

            <div class="campo-filtro">
                <label for="filtroTipo">Mostrar</label>
                <select id="filtroTipo" name="filtroTipo">
                    <option value="todos">Todos los nodos</option>
                    <option value="inactivo">Solo inactivos</option>
                    <option value="error">Solo lecturas erróneas</option>
                </select>
            </div>

            <div class="campo-filtro">
                <label for="ordenarPor">Ordenar por</label>
                <select id="ordenarPor" name="ordenarPor">
                    <option value="ultima_medida">Última medida</option>
                    <option value="tiempo_problema">Tiempo con problema</option>
                    <option value="id_placa">Id de nodo</option>
                </select>
            </div>

            <button class="btn-actualizar" id="btnActualizarInforme">
                Actualizar informe
            </button>
        </div>

        <!--
        /**
         * @subsection Resumen del informe
         * @brief Tarjetas resumen con métricas globales de nodos.
         */
        -->
        <div class="resumen-informe">

            <div class="tarjeta-resumen nodos-totales">
                <span class="resumen-label">Nodos totales</span>
                <span class="resumen-valor" id="resumenTotal">–</span>
            </div>

            <div class="tarjeta-resumen nodos-inactivos">
                <span class="resumen-label">Nodos inactivos</span>
                <span class="resumen-valor" id="resumenInactivos">–</span>
            </div>

            <div class="tarjeta-resumen nodos-erroneos">
                <span class="resumen-label">Con lecturas erróneas</span>
                <span class="resumen-valor" id="resumenErrores">–</span>
            </div>
        </div>
    </div>

    <!--
    /**
     * @section Tabla principal
     * @brief Tabla con el detalle de nodos que presentan incidencias.
     *
     * El contenido del `<tbody>` se rellena dinámicamente
     * desde el archivo `js/informe_nodos.js`.
     */
    -->
    <div class="tabla-informe-nodos-container">
        <h3>Detalle de nodos con incidencias</h3>

        <!-- Barra superior de control de longitud -->
        <div class="tabla-toolbar">
            <div class="tabla-length">
                <label for="selectLongitud">Mostrar</label>
                <select id="selectLongitud" name="selectLongitud">
                    <option value="5">5</option>
                    <option value="15" selected>15</option>
                    <option value="20">20</option>
                    <option value="100">100</option>
                </select>
                <span>registros</span>
            </div>
        </div>

        <div class="tabla-scroll-wrapper">
            <table class="tabla-informe-nodos">
                <thead>
                    <tr>
                        <th>Id nodo</th>
                        <th>Ubicación</th>
                        <th>Última medida</th>
                        <th>Estado</th>
                        <th>Tiempo de inactividad</th>
                    </tr>
                </thead>
                <tbody id="tbodyNodos">
                    <!--
                    /**
                     * @brief Filas generadas dinámicamente por JavaScript.
                     */
                    -->
                </tbody>
            </table>
        </div>

        <!-- Nota aclaratoria -->
        <p class="nota-informe">
            * Para las pruebas de clase, se considera un nodo inactivo si no envía medidas
            durante más de 5 minutos. En producción este umbral sería de 24 horas.
        </p>
    </div>
</div>
