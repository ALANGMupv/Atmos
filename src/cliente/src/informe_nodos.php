<?php
// informeNodos.php
// Vista del informe de nodos (T019). Por ahora solo front + estilos.
// La lógica de /estadoNodos la manejas en el backend Node/Express.
?>
<link rel="stylesheet" href="css/informe_nodos.css">
<script src="js/informe_nodos.js" defer></script>

<div class="home-container"><!-- igual que en userPage.php -->
    <div class="saludo-container">
        <h2>Header o algo?</h2>
        <p>En que pagina iria esto admin?</p>
    </div>

    <div class="panel-informe-nodos">
        <!-- FILTROS + RESUMEN SUPERIOR -->
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

        <!-- TABLA PRINCIPAL -->
        <div class="tabla-informe-nodos-container">
            <h3>Detalle de nodos con incidencias</h3>

            <!-- Barra superior de control tabla -->
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
                            <th>Tiempo con problema</th>
                        </tr>
                    </thead>
                    <tbody id="tbodyNodos">
                        <!-- Se rellenará dinámicamente desde js/informe_nodos.js -->
                    </tbody>
                </table>
            </div>

            <p class="nota-informe">
                * Para las pruebas de clase, se considera un nodo inactivo si no envía medidas
                durante más de 5 minutos. En producción este umbral sería de 24 horas.
            </p>
        </div>
    </div>
</div>
