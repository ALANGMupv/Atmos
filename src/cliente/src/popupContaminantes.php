<!--
/**
 * @file popupContaminantes.php
 * @brief Popup informativo sobre contaminantes atmosféricos.
 *
 * Este componente muestra información educativa sobre los principales
 * contaminantes monitorizados por ATMOS:
 *  - CO (Monóxido de carbono)
 *  - NO₂ (Dióxido de nitrógeno)
 *  - O₃ (Ozono troposférico)
 *  - SO₂ (Dióxido de azufre)
 *
 * Incluye:
 *  - Tabla de rangos de calidad del aire (ppm).
 *  - Información descriptiva de cada contaminante.
 *  - Consejos de salud.
 *
 * Se utiliza como popup reutilizable en:
 *  - Mapa público
 *  - Mapa de usuarios registrados
 *
 * Controlado por:
 *  - JS de popups (data-popup / cerrar-popup)
 *
 * @author Equipo ATMOS
 * @version 1.0
 */
-->
<section id="popupContaminantes" class="popup-info-container">

  <div class="popup-container">

    <!--
    /**
     * @brief Botón de cierre del popup.
     *
     * Al pulsar, elimina la clase `activo` del contenedor padre.
     */
    -->
    <button class="cerrar-popup">
      <img src="img/cerrarIcono.svg" alt="Cerrar">
    </button>

    <!--
    /**
     * @brief Título principal del popup.
     */
    -->
    <h2 class="popup-titulo">Información de contaminantes</h2>

    <!--
    /**
     * @section Tabla de rangos
     * @brief Tabla con los rangos oficiales de calidad del aire en ppm.
     *
     * Los colores representan niveles de riesgo:
     *  - Verde: Bueno
     *  - Amarillo: Moderado
     *  - Naranja: Insalubre
     *  - Rojo: Malo
     */
    -->
    <div class="popup-tabla">

      <h3>Rangos de calidad del aire (ppm)</h3>

      <table>
        <thead>
          <tr>
            <th>Gas</th>
            <th>Bueno</th>
            <th>Moderado</th>
            <th>Insalubre</th>
            <th>Malo</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>CO</td>
            <td class="verde">0 – 1.7</td>
            <td class="amarillo">1.7 – 4.4</td>
            <td class="naranja">4.4 – 8.7</td>
            <td class="rojo">&gt; 8.7</td>
          </tr>
          <tr>
            <td>NO₂</td>
            <td class="verde">0 – 0.02</td>
            <td class="amarillo">0.02 – 0.05</td>
            <td class="naranja">0.05 – 0.11</td>
            <td class="rojo">&gt; 0.11</td>
          </tr>
          <tr>
            <td>O₃</td>
            <td class="verde">0 – 0.03</td>
            <td class="amarillo">0.03 – 0.06</td>
            <td class="naranja">0.06 – 0.09</td>
            <td class="rojo">&gt; 0.09</td>
          </tr>
          <tr>
            <td>SO₂</td>
            <td class="verde">0 – 0.008</td>
            <td class="amarillo">0.008 – 0.02</td>
            <td class="naranja">0.02 – 0.04</td>
            <td class="rojo">&gt; 0.04</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!--
    /**
     * @section Información detallada
     * @brief Descripción individual de cada contaminante.
     *
     * Cada bloque incluye:
     *  - Origen principal
     *  - Efectos sobre la salud
     *  - Recomendación preventiva
     */
    -->
    <div class="popup-scroll">

      <!--
      /**
       * @subsection NO2
       * @brief Información sobre el dióxido de nitrógeno (NO₂).
       */
      -->
      <div class="contaminante-info">
        <h4>NO₂ – Dióxido de nitrógeno</h4>
        <p><strong>Origen:</strong> Tráfico rodado, motores diésel y procesos industriales.</p>
        <p><strong>Efectos:</strong> Irritación respiratoria, empeora el asma y reduce la función pulmonar.</p>
        <p><strong>Consejo:</strong> Evita ejercicio intenso en exteriores con niveles altos.</p>

        <img src="img/car.svg" alt="Tráfico y vehículos" class="contaminante-icono">
      </div>

      <!--
      /**
       * @subsection CO
       * @brief Información sobre el monóxido de carbono (CO).
       */
      -->
      <div class="contaminante-info">
        <h4>CO – Monóxido de carbono</h4>
        <p><strong>Origen:</strong> Combustión incompleta en vehículos y sistemas de calefacción.</p>
        <p><strong>Efectos:</strong> Reduce el transporte de oxígeno en sangre; mareos y fatiga.</p>
        <p><strong>Consejo:</strong> Ventila espacios cerrados y revisa sistemas de combustión.</p>

        <img src="img/oil.svg" alt="Combustión" class="contaminante-icono">
      </div>

      <!--
      /**
       * @subsection O3
       * @brief Información sobre el ozono troposférico (O₃).
       */
      -->
      <div class="contaminante-info">
        <h4>O₃ – Ozono troposférico</h4>
        <p><strong>Origen:</strong> Reacciones fotoquímicas entre NOx, COV y radiación solar.</p>
        <p><strong>Efectos:</strong> Tos, dolor torácico y dificultad respiratoria.</p>
        <p><strong>Consejo:</strong> Evita actividades al aire libre en horas de máxima radiación.</p>

        <img src="img/sun.svg" alt="Radiación solar" class="contaminante-icono">
      </div>

      <!--
      /**
       * @subsection SO2
       * @brief Información sobre el dióxido de azufre (SO₂).
       */
      -->
      <div class="contaminante-info">
        <h4>SO₂ – Dióxido de azufre</h4>
        <p><strong>Origen:</strong> Procesos industriales y combustión de carbón.</p>
        <p><strong>Efectos:</strong> Irritación ocular y respiratoria; puede formar ácido sulfúrico.</p>
        <p><strong>Consejo:</strong> Evita zonas industriales activas y usa protección si es necesario.</p>

        <img src="img/fabrica.svg" alt="Industria" class="contaminante-icono">
      </div>

    </div>
  </div>
</section>
