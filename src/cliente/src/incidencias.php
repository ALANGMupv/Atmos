<?php
session_start();

// Comprobar sesión activa
if (!isset($_SESSION['usuario'])) {
    header("Location: login.php");
    exit();
}

$usuario = $_SESSION['usuario'];
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
    <?php include __DIR__ . '/partials/headerLogueado.php'; ?>

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
                            <!-- Cargado dinámicamente por JS -->
                            <div class="loading-state" style="padding: 20px; text-align: center; color: #666;">
                                <i class="fa-solid fa-spinner fa-spin"></i> Cargando incidencias...
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
                        <form class="send-incidence-form" id="formEnviar">
                            <div class="form-group">
                                <input type="text" class="form-control" id="asuntoInput"
                                    placeholder="Introduce un título para tu incidencia">
                            </div>
                            <div class="form-group">
                                <textarea class="form-control" id="mensajeInput"
                                    placeholder="Describe tu incidencia con detalle..." rows="3"></textarea>
                            </div>
                            <button type="submit" class="btn btn-primary btn-block">Enviar Incidencia</button>
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
                            <div class="empty-state-detail" id="emptyStateDetail"
                                style="text-align: center; margin-top: 50px; color: #9CA3AF;">
                                <i class="fa-regular fa-folder-open" style="font-size: 40px; margin-bottom: 20px;"></i>
                                <p>Selecciona una incidencia para ver los detalles.</p>
                            </div>

                            <div id="contentDetailWrapper" style="display: none;">
                                <h2 class="detail-title-text" id="detailTitle"></h2>
                                <p class="detail-full-text" id="detailDesc"></p>
                            </div>
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
                            <div class="status-hero pending" id="statusHeroPlaceholder"
                                style="background: #FAFAFA; border-style: solid;">
                                <div class="status-icon-large" style="background: #F1F5F9; color:#94A3B8;">
                                    <i class="fa-solid fa-inbox"></i>
                                </div>
                                <h4 class="status-title" style="color:#94A3B8;">Nada seleccionado</h4>
                                <p class="status-desc">
                                    Haz clic en una incidencia de la izquierda.
                                </p>
                            </div>
                        </div>
                    </div>

                </div>
            </div>

        </div>
    </main>

    <script>
        window.ID_USUARIO = <?= json_encode($usuario['id_usuario']); ?>;
    </script>
    <script src="js/incidencias.js"></script>

</body>

</html>