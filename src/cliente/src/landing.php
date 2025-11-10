<!doctype html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>Landing Page - Atmos</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link
        href="https://fonts.googleapis.com/css2?family=Poppins:ital,wght@0,100;0,200;0,300;0,400;0,500;0,600;0,700;0,800;0,900;1,100;1,200;1,300;1,400;1,500;1,600;1,700;1,800;1,900&display=swap"
        rel="stylesheet">

    <!-- CSS -->
    <link rel="stylesheet" href="css/landing.css">

</head>

<body>
    <?php include __DIR__ . '/partials/header.php'; ?>
    <main>
        <section class="landing-container">

            <div class="Container-info">
                <!-- Logo -->
                <img src="img/LogoAtmosFull.png" alt="Logo Atmos" class="logo">

                <!-- Texto de introducción -->
                <h1 class="texto-intro">
                    Visualiza el aire que respiras <br> en todo momento
                </h1>
            </div>

            <div class="Container-ctas">
                <button class="btn-login" onclick="window.location.href='login.php'">INICIAR SESÓN</button>
                <button class="btn-registro" onclick="window.location.href='registro.php'">REGISTRARME</button>
                <a href="index.php" class="acceso-invitado">Acceder como invitado</a>
            </div>




        </section>
    </main>

</body>

</html>