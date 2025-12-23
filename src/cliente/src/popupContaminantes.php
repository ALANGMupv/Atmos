<section id="popupContaminantes" class="popup-info-container">

  <div class="popup-container">

    <!-- CERRAR -->
    <button class="cerrar-popup">
      <img src="img/cerrarIcono.svg" alt="Cerrar">
    </button>

    <!-- TÍTULO -->
    <h2 class="popup-titulo">Información de contaminantes</h2>

    <!-- ================= TABLA DE RANGOS ================= -->
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

    <!-- ================= SCROLL INFO ================= -->
    <div class="popup-scroll">

      <!-- NO2 -->
      <div class="contaminante-info">
        <h4>NO₂ – Dióxido de nitrógeno</h4>
        <p><strong>Origen:</strong> Tráfico rodado, motores diésel y procesos industriales.</p>
        <p><strong>Efectos:</strong> Irritación respiratoria, empeora el asma y reduce la función pulmonar.</p>
        <p><strong>Consejo:</strong> Evita ejercicio intenso en exteriores con niveles altos.</p>

        <img src="img/car.svg" alt="Tráfico y vehículos" class="contaminante-icono">
      </div>

      <!-- CO -->
      <div class="contaminante-info">
        <h4>CO – Monóxido de carbono</h4>
        <p><strong>Origen:</strong> Combustión incompleta en vehículos y sistemas de calefacción.</p>
        <p><strong>Efectos:</strong> Reduce el transporte de oxígeno en sangre; mareos y fatiga.</p>
        <p><strong>Consejo:</strong> Ventila espacios cerrados y revisa sistemas de combustión.</p>

        <img src="img/oil.svg" alt="Combustión" class="contaminante-icono">
      </div>

      <!-- O3 -->
      <div class="contaminante-info">
        <h4>O₃ – Ozono troposférico</h4>
        <p><strong>Origen:</strong> Reacciones fotoquímicas entre NOx, COV y radiación solar.</p>
        <p><strong>Efectos:</strong> Tos, dolor torácico y dificultad respiratoria.</p>
        <p><strong>Consejo:</strong> Evita actividades al aire libre en horas de máxima radiación.</p>

        <img src="img/sun.svg" alt="Radiación solar" class="contaminante-icono">
      </div>

      <!-- SO2 -->
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
