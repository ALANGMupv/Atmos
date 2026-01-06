<?php
session_start();

// Simulación de rol de admin (Idealmente se comprueba en BD)
// if (!isset($_SESSION['usuario']) || $_SESSION['usuario']['id_rol'] != 2) {
//     header("Location: login.php");
//     exit();
// }
// $usuario = $_SESSION['usuario'];
// Para pruebas frontend, usamos datos dummy si no hay sesión
$usuario = $_SESSION['usuario'] ?? ['nombre' => 'Administrador', 'id_usuario' => 999];
?>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Panel Admin Incidencias - Atmos</title>

    <!-- Estilos Globales -->
    <link rel="stylesheet" href="css/index.css">
    <link rel="stylesheet" href="css/header.css">
    <link rel="stylesheet" href="css/buttons.css">
    <link rel="stylesheet" href="css/footer.css">

    <!-- Estilos Específicos Admin -->
    <link rel="stylesheet" href="css/adminIncidencias.css">

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
    <?php
    $active = 'incidencias';
    include __DIR__ . '/partials/headerEmpresas.php';
    ?>

    <main class="incidencias-wrap">
        <div class="incidencias-grid">

            <!-- COLUMNA IZQUIERDA: LISTA DE TODAS LAS INCIDENCIAS -->
            <div class="col-left">

                <div class="card-box card-box-admin" style="height: 100%;">
                    <div class="card-header-gradient">
                        <div class="header-icon-title">
                            <i class="fa-solid fa-list-check"></i>
                            <h2>Bandeja de Entrada</h2>
                        </div>
                    </div>

                    <!-- Filtros rápidos (Mockup) -->
                    <div style="padding: 10px 15px; border-bottom: 1px solid #eee; display:flex; gap:10px;">
                        <button class="btn-filter active"
                            style="font-size:12px; padding:4px 8px; border-radius:12px; border:1px solid #ddd; background:#fff;">Todas</button>
                        <button class="btn-filter"
                            style="font-size:12px; padding:4px 8px; border-radius:12px; border:1px solid #ddd; background:#fff;">Pendientes</button>
                        <button class="btn-filter"
                            style="font-size:12px; padding:4px 8px; border-radius:12px; border:1px solid #ddd; background:#fff;">Resueltas</button>
                    </div>

                    <div class="card-body p-0">
                        <div class="incidencias-list" id="adminIncidenceList">
                            <!-- Items cargados dinámicamente por js/adminIncidencias.js -->
                            <div class="loading-state" style="padding: 20px; text-align: center; color: #666;">
                                <i class="fa-solid fa-spinner fa-spin"></i> Cargando...
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- COLUMNA DERECHA: DETALLE Y ACCIONES -->
            <div class="col-right">
                <div class="card-box card-box-right">

                    <!-- Sección Título y Mensaje de Usuario -->
                    <div class="right-section" style="flex: 1;">
                        <div class="card-header-gradient">
                            <div class="header-icon-title">
                                <i class="fa-solid fa-user-pen"></i>
                                <h3>Mensaje del Usuario</h3>
                            </div>
                        </div>
                        <div class="card-body" id="detailContent">
                            <div class="incidence-user-info" id="detailUser"
                                style="font-size: 14px; margin-bottom: 10px;">
                                <i class="fa-solid fa-user"></i> Juan Pérez
                            </div>
                            <h2 class="detail-title-text" id="detailTitle">Fallo en sensor norte</h2>
                            <p class="detail-full-text" id="detailDesc">
                                Hola, he notado que el sensor del sector norte ha dejado de enviar datos desde ayer por
                                la noche. He intentado reiniciarlo pero no responde a los comandos remotos.
                            </p>
                        </div>
                    </div>

                    <!-- Sección Respuesta Admin -->
                    <div class="right-section" style="flex: 1;">
                        <div class="card-header-gradient">
                            <div class="header-icon-title">
                                <i class="fa-solid fa-reply"></i>
                                <h3>Gestionar Incidencia</h3>
                            </div>
                        </div>
                        <div class="card-body">
                            <form id="adminActionForm" class="action-panel">
                                <label style="font-size:12px; color:#666; font-weight:600;">TU RESPUESTA:</label>
                                <textarea class="action-textarea" id="responseInput"
                                    placeholder="Escribe aquí la respuesta al usuario..."></textarea>

                                <div class="action-controls">
                                    <div class="status-selector">
                                        <button type="button" class="btn-action btn-resolve"
                                            onclick="setStatus('resolved', this)">
                                            <i class="fa-solid fa-check"></i> Resolver
                                        </button>
                                        <button type="button" class="btn-action btn-reject"
                                            onclick="setStatus('rejected', this)">
                                            <i class="fa-solid fa-xmark"></i> Rechazar
                                        </button>
                                    </div>
                                    <button type="submit" class="btn btn-primary btn-submit-response">
                                        Enviar Respuesta <i class="fa-solid fa-paper-plane"></i>
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>

                </div>
            </div>

        </div>
    </main>

    <script>
        let selectedStatus = null;

        function setStatus(status, btn) {
            // Reset clases
            document.querySelectorAll('.btn-action').forEach(b => b.classList.remove('active'));
            // Activar actual
            btn.classList.add('active');
            selectedStatus = status;
        }

        function selectIncidence(element) {
            // 1. Quitar active de todos
            document.querySelectorAll('.incidence-item').forEach(item => item.classList.remove('active'));
            element.classList.add('active');

            // 2. Obtener datos
            const user = element.dataset.user;
            const title = element.dataset.title;
            const desc = element.dataset.desc;
            const status = element.dataset.status;
            const response = element.dataset.response || "";

            // 3. Renderizar arriba
            document.getElementById('detailUser').innerHTML = `<i class="fa-solid fa-user"></i> ${user}`;
            document.getElementById('detailTitle').textContent = title;
            document.getElementById('detailDesc').textContent = desc;

            // 4. Preparar formulario abajo
            const textarea = document.getElementById('responseInput');
            textarea.value = response;

            // Reset status buttons
            document.querySelectorAll('.btn-action').forEach(b => b.classList.remove('active'));
            selectedStatus = null;

            if (status === 'resolved') {
                // Si ya está resuelta, podríamos bloquear edición o marcar el botón
                document.querySelector('.btn-resolve').classList.add('active');
            } else if (status === 'rejected') {
                document.querySelector('.btn-reject').classList.add('active');
            }
        }

        // Mockup submit
        document.getElementById('adminActionForm').addEventListener('submit', (e) => {
            e.preventDefault();
            if (!selectedStatus && document.getElementById('responseInput').value.trim() === "") {
                alert("Por favor escribe una respuesta o cambia el estado.");
                return;
            }
            alert("Respuesta enviada y estado actualizado (Simulación Frontend).");
        });
    </script>
    <script src="js/adminIncidencias.js"></script>

</body>

</html>