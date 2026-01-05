<?php
session_start();
?>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Incidencias - Atmos</title>

    <!-- Estilos Globales -->
    <link rel="stylesheet" href="css/index.css">
    <link rel="stylesheet" href="css/header.css">
    <link rel="stylesheet" href="css/buttons.css">
    <link rel="stylesheet" href="css/footer.css">

    <!-- Estilos Específicos -->
    <link rel="stylesheet" href="css/incidencias.css">

    <!-- Fuentes y Iconos -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link
        href="https://fonts.googleapis.com/css2?family=Roboto:wght@100;300;400;500;700;900&family=Open+Sans:wght@300;400;600;700&display=swap"
        rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>

<body>

    <!-- HEADER -->
    <?php include __DIR__ . '/partials/header.php'; ?>

    <main class="incidencias-wrap">
        <div class="incidencias-grid">

            <!-- COLUMNA IZQUIERDA -->
            <div class="col-left">

                <!-- 1. Incidencias (Arriba) -->
                <div class="card-box">
                    <div class="card-header-gradient">
                        <h2>Incidencias</h2>
                    </div>
                    <div class="card-body p-0">
                        <div class="incidencias-list" id="incidenceList">

                            <!-- Item 1: Pendiente -->
                            <div class="incidence-item active" onclick="selectIncidence(this)"
                                data-title="Fallo en sensor norte" data-date="9:41 - 08/09/2025" data-status="pending"
                                data-desc="Hola, he notado que el sensor del sector norte ha dejado de enviar datos desde ayer por la noche. He intentado reiniciarlo pero no responde a los comandos remotos.<br><br>¿Podrían verificar si es un problema de conectividad o de hardware? Adjunto logs si es necesario.">
                                <div class="incidence-header">
                                    <h4 class="incidence-title-item">Fallo en sensor norte</h4>
                                    <div class="incidence-meta">
                                        <span class="incidence-time">9:41 - 08/09/2025</span>
                                        <span class="status-dot yellow" title="Pendiente"></span>
                                    </div>
                                </div>
                                <p class="incidence-preview">Hola, he notado que el sensor del sector norte ha dejado de
                                    enviar datos desde ayer por la noche...</p>
                            </div>

                            <!-- Item 2: Resuelta -->
                            <div class="incidence-item" onclick="selectIncidence(this)"
                                data-title="Solicitud de mantenimiento" data-date="14:20 - 05/09/2025"
                                data-status="resolved"
                                data-desc="Me gustaría programar una revisión preventiva para los sensores de la zona industrial, ya que han pasado 6 meses desde la última revisión.<br><br>Los sensores parecen funcionar bien, pero el protocolo exige calibración semestral."
                                data-resolution="Mantenimiento programado y realizado con éxito. Se han calibrado todos los sensores de la zona industrial. Próxima revisión en 03/2026.">
                                <div class="incidence-header">
                                    <h4 class="incidence-title-item">Solicitud de mantenimiento</h4>
                                    <div class="incidence-meta">
                                        <span class="incidence-time">14:20 - 05/09/2025</span>
                                        <span class="status-dot green" title="Resuelta"></span>
                                    </div>
                                </div>
                                <p class="incidence-preview">Me gustaría programar una revisión preventiva para los
                                    sensores de la zona industrial...</p>
                            </div>

                            <!-- Item 3: Pendiente -->
                            <div class="incidence-item" onclick="selectIncidence(this)"
                                data-title="Error de conexión WiFi" data-date="10:05 - 01/09/2025" data-status="pending"
                                data-desc="El dispositivo muestra una luz roja parpadeante y no se conecta a la red WiFi del edificio. He comprobado el router y funciona correctamente con otros equipos.">
                                <div class="incidence-header">
                                    <h4 class="incidence-title-item">Error de conexión WiFi</h4>
                                    <div class="incidence-meta">
                                        <span class="incidence-time">10:05 - 01/09/2025</span>
                                        <span class="status-dot yellow"></span>
                                    </div>
                                </div>
                                <p class="incidence-preview">El dispositivo muestra una luz roja parpadeante y no se
                                    conecta a la red WiFi...</p>
                            </div>

                            <!-- Item 4: Resuelta (Ejemplo Extra) -->
                            <div class="incidence-item" onclick="selectIncidence(this)"
                                data-title="Lecturas erróneas CO2" data-date="11:30 - 28/08/2025" data-status="resolved"
                                data-desc="El sensor de CO2 está reportando valores de 0ppm constantemente, lo cual es imposible. Probablemente sea fallo del sensor."
                                data-resolution="Sensor reemplazado remotamente por backup y ticket enviado a equipo técnico para sustitución física. El problema de datos ha sido mitigado.">
                                <div class="incidence-header">
                                    <h4 class="incidence-title-item">Lecturas erróneas CO2</h4>
                                    <div class="incidence-meta">
                                        <span class="incidence-time">11:30 - 28/08/2025</span>
                                        <span class="status-dot green"></span>
                                    </div>
                                </div>
                                <p class="incidence-preview">El sensor de CO2 está reportando valores de 0ppm
                                    constantemente...</p>
                            </div>

                            <!-- Item 5: Relleno para scroll -->
                            <div class="incidence-item" onclick="selectIncidence(this)"
                                data-title="Consulta facturación" data-date="09:00 - 20/08/2025" data-status="pending"
                                data-desc="Necesito la factura del mes pasado con el desglose de IVA corregido.">
                                <div class="incidence-header">
                                    <h4 class="incidence-title-item">Consulta facturación</h4>
                                    <div class="incidence-meta">
                                        <span class="incidence-time">09:00 - 20/08/2025</span>
                                        <span class="status-dot yellow"></span>
                                    </div>
                                </div>
                                <p class="incidence-preview">Necesito la factura del mes pasado con el desglose de IVA
                                    corregido...</p>
                            </div>

                        </div>
                    </div>
                </div>

                <!-- 2. Enviar una incidencia (Abajo) -->
                <div class="card-box">
                    <div class="card-header-gradient">
                        <div class="header-icon-title">
                            <i class="fa-regular fa-note-sticky"></i>
                            <h2>Enviar una incidencia</h2>
                        </div>
                    </div>
                    <div class="card-body">
                        <form class="send-incidence-form">
                            <div class="form-group">
                                <input type="text" class="form-control"
                                    placeholder="Introduce un título para tu incidencia">
                            </div>
                            <div class="form-group">
                                <textarea class="form-control" placeholder="Describe tu incidencia con detalle..."
                                    rows="3"></textarea>
                            </div>
                            <button type="button" class="btn btn-primary btn-block">Enviar Incidencia</button>
                        </form>
                    </div>
                </div>

            </div>

            <!-- COLUMNA DERECHA -->
            <div class="col-right">
                <div class="card-box card-box-right">

                    <!-- Sección Título -->
                    <div class="right-section">
                        <div class="card-header-gradient">
                            <div class="header-icon-title">
                                <i class="fa-solid fa-paperclip"></i>
                                <h3>Título de la incidencia</h3>
                            </div>
                        </div>
                        <div class="card-body" id="detailContent">
                            <!-- Contenido del título (Detalle) -->
                            <h2 class="detail-title-text" id="detailTitle">Fallo en sensor norte</h2>
                            <p class="detail-full-text" id="detailDesc">
                                Hola, he notado que el sensor del sector norte ha dejado de enviar datos desde ayer por
                                la noche. He intentado reiniciarlo pero no responde a los comandos remotos.
                                <br><br>
                                ¿Podrían verificar si es un problema de conectividad o de hardware? Adjunto logs si es
                                necesario.
                            </p>
                        </div>
                    </div>

                    <!-- Sección Resolución -->
                    <div class="right-section">
                        <div class="card-header-gradient">
                            <div class="header-icon-title">
                                <i class="fa-solid fa-triangle-exclamation"></i>
                                <h3>Resolución</h3>
                            </div>
                        </div>
                        <div class="card-body" id="resolutionContent">
                            <!-- Contenido Dinámico (Status Hero) -->
                            <div class="status-hero pending">
                                <div class="status-icon-large">
                                    <i class="fa-regular fa-clock"></i>
                                </div>
                                <h4 class="status-title">Incidencia en revisión</h4>
                                <p class="status-desc">
                                    Nuestro equipo está analizando el caso reportado.<br>
                                    Te notificaremos en cuanto haya novedades.
                                </p>
                            </div>
                        </div>
                    </div>

                </div>
            </div>

        </div>
    </main>

    <script>
        function selectIncidence(element) {
            // 1. Quitar active de todos
            document.querySelectorAll('.incidence-item').forEach(item => item.classList.remove('active'));
            // 2. Poner active al clickado
            element.classList.add('active');

            // 3. Obtener datos
            const title = element.dataset.title;
            const desc = element.dataset.desc;
            const status = element.dataset.status;
            const resolution = element.dataset.resolution;

            // 4. Actualizar lado derecho
            document.getElementById('detailTitle').textContent = title;
            document.getElementById('detailDesc').innerHTML = desc;

            // 5. Lógica de Resolución (Status Hero)
            const resContainer = document.getElementById('resolutionContent');

            if (status === 'pending') {
                resContainer.innerHTML = `
                    <div class="status-hero pending">
                        <div class="status-icon-large">
                            <i class="fa-regular fa-clock"></i>
                        </div>
                        <h4 class="status-title">Incidencia en revisión</h4>
                        <p class="status-desc">
                            Nuestro equipo está analizando el caso reportado.<br>
                            Te notificaremos en cuanto haya novedades.
                        </p>
                    </div>
                `;
            } else if (status === 'resolved') {
                resContainer.innerHTML = `
                    <div class="status-hero resolved">
                        <div class="status-icon-large">
                            <i class="fa-solid fa-check"></i>
                        </div>
                        <h4 class="status-title">Incidencia Resuelta</h4>
                        <p class="status-desc">${resolution}</p>
                    </div>
                `;
            }
        }
    </script>

</body>

</html>