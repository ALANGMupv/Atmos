<!doctype html>
<html lang="es">

<head>
    <!--
    /**
     * @file index.php
     * @brief Página principal (Landing Page) del proyecto ATMOS.
     *
     * Esta página actúa como punto de entrada público a la plataforma ATMOS.
     * Presenta el proyecto, su propósito, funcionalidades principales
     * y enlaces de acceso al registro y al mapa público.
     *
     * Secciones principales:
     *  - Hero de bienvenida
     *  - Explicación del funcionamiento
     *  - Funcionalidades según tipo de usuario
     *  - CTA de compra de sensor
     *
     * Dependencias:
     *  - partials/header.php
     *  - partials/footer.php
     *  - CSS globales y específicos
     *
     * @author Equipo ATMOS
     * @version 1.0
     */
    -->

    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>ATMOS — Respira con datos reales</title>

    <!--
    /**
     * @section Fuentes
     * @brief Fuentes tipográficas utilizadas en la landing.
     */
    -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Open+Sans:wght@300;400;600&family=Roboto:wght@600&display=swap"
        rel="stylesheet">

    <!--
    /**
     * @section Estilos
     * @brief Hojas de estilo globales y componentes reutilizables.
     */
    -->
    <link rel="stylesheet" href="css/index.css?v=1.0.6">
    <link rel="stylesheet" href="css/buttons.css?v=1.0.3">
    <link rel="stylesheet" href="css/header.css?v=1.0.5">
</head>

<body>

    <?php
    /**
     * @brief Inclusión del header público.
     *
     * Contiene el menú de navegación principal visible para usuarios
     * no autenticados.
     */
    include __DIR__ . '/partials/header.php';
    ?>

    <main>

        <!--
    /**
     * @section Hero
     * @brief Sección principal de bienvenida.
     *
     * Presenta el mensaje principal del proyecto ATMOS
     * y llamadas a la acción (registro y mapa público).
     */
    -->
        <section id="hero" class="section section-hero">
            <div class="wrap hero-content">
                <h1 class="hero-title">
                    Visualiza el aire que<br>
                    <span class="line2">respiras en todo momento</span>
                </h1>

                <p class="hero-sub">
                    Únete a una comunidad colaborativa que mide la calidad del aire en tiempo real.
                    Tus datos ayudan a crear ciudades más saludables.
                </p>

                <div class="cta-row centered">
                    <a href="registro.php" class="btn btn-primary btn-lg">Comienza ahora</a>
                    <a href="mapa.php" class="btn btn-outline btn-lg">Ver mapa público</a>
                </div>
            </div>

            <!-- Scroll hacia la siguiente sección -->
            <a href="#funciona" class="scroll-down">
                <img src="img/ArrowDownCircle.svg" alt="Desplázate hacia abajo" class="scroll-icon">
            </a>
        </section>

        <!--
    /**
     * @section Funciona
     * @brief Explicación del funcionamiento de ATMOS.
     *
     * Describe el flujo general del sistema y la participación
     * ciudadana en la recolección y visualización de datos.
     */
    -->
        <section id="funciona" class="section section-funciona">
            <div class="wrap funciona-content">

                <!-- Bloque: Cómo funciona -->
                <h2 class="funciona-title">¿Cómo funciona Atmos?</h2>
                <p class="funciona-sub">
                    Un sistema simple y efectivo basado en la participación ciudadana
                </p>

                <div class="funciona-grid">
                    <img src="img/Tarjeta1Landing.png" alt="App móvil ATMOS" class="funciona-icon">
                    <img src="img/Recolecta Datos.png" alt="Recolección de datos ATMOS" class="funciona-icon">
                    <img src="img/Ver Mapas.png" alt="Ver mapas ATMOS" class="funciona-icon">
                </div>

                <div class="section-divider"></div>

                <!-- Bloque: Funcionalidades -->
                <h2 class="funcionalidades-title">Funcionalidades para cada usuario</h2>
                <p class="funcionalidades-sub">
                    Diferentes niveles de acceso según tu participación
                </p>

                <div class="funcionalidades-grid">
                    <img src="img/Usuario Visitante.png" alt="Usuario visitante ATMOS" class="funcionalidades-icon">
                    <img src="img/Usuario Registrado.png" alt="Usuario registrado ATMOS" class="funcionalidades-icon">
                </div>
            </div>
        </section>

        <!--
    /**
     * @section CTA Sensor
     * @brief Llamada a la acción para la compra del sensor.
     *
     * Invita al usuario a adquirir un sensor físico
     * para contribuir activamente a la red ATMOS.
     */
    -->
        <!--
    /**
     * @section FAQ
     * @brief Preguntas Frecuentes.
     *
     * Sección desplegable con información útil para el usuario.
     */
    -->
        <section id="faq" class="section section-faq">
            <div class="wrap">
                <h2 class="faq-title">FAQ – Preguntas Frecuentes</h2>

                <!-- 1. ¿Qué es el sistema ATMOS? -->
                <details class="faq-item">
                    <summary>1. ¿Qué es el sistema ATMOS?</summary>
                    <div class="faq-content">
                        <p>ATMOS es un sistema de monitorización ambiental que permite medir y visualizar parámetros
                            como temperatura y niveles de contaminación (por ejemplo, ozono) mediante sensores físicos,
                            una aplicación móvil Android y un portal web. El sistema está diseñado para ofrecer
                            información en tiempo real y datos históricos de calidad ambiental.</p>
                    </div>
                </details>

                <!-- 2. ¿Qué componentes forman el sistema ATMOS? -->
                <details class="faq-item">
                    <summary>2. ¿Qué componentes forman el sistema ATMOS?</summary>
                    <div class="faq-content">
                        <p>El sistema está compuesto por tres elementos principales:</p>
                        <ul>
                            <li><strong>Sensores ambientales (nodos):</strong> dispositivos basados en Arduino o ESP que
                                recogen datos ambientales y los transmiten mediante Bluetooth BLE.</li>
                            <li><strong>Aplicación móvil Android:</strong> permite detectar sensores cercanos, consultar
                                mapas de contaminación, visualizar mediciones y reportar incidencias.</li>
                            <li><strong>Portal web:</strong> plataforma centralizada para la visualización de datos
                                históricos, gestión de incidencias y administración del sistema.</li>
                        </ul>
                    </div>
                </details>

                <!-- 3. ¿Qué tipos de usuarios existen en ATMOS? -->
                <details class="faq-item">
                    <summary>3. ¿Qué tipos de usuarios existen en ATMOS?</summary>
                    <div class="faq-content">
                        <p>Existen dos tipos de usuarios:</p>
                        <ul>
                            <li>Usuario básico</li>
                            <li>Usuario administrador</li>
                        </ul>
                        <p>Cada tipo de usuario dispone de diferentes permisos y funcionalidades dentro del sistema.</p>
                    </div>
                </details>

                <!-- 4. ¿Qué puede hacer un usuario básico? -->
                <details class="faq-item">
                    <summary>4. ¿Qué puede hacer un usuario básico?</summary>
                    <div class="faq-content">
                        <p>Un usuario básico puede:</p>
                        <ul>
                            <li>Registrarse y acceder al sistema.</li>
                            <li>Editar su perfil personal.</li>
                            <li>Visualizar datos ambientales y mapas de contaminación.</li>
                            <li>Reportar incidencias relacionadas con los sensores.</li>
                            <li>Consultar el estado y resolución de sus incidencias.</li>
                        </ul>
                    </div>
                </details>

                <!-- 5. ¿Qué funcionalidades adicionales tiene un administrador? -->
                <details class="faq-item">
                    <summary>5. ¿Qué funcionalidades adicionales tiene un administrador?</summary>
                    <div class="faq-content">
                        <p>El administrador puede:</p>
                        <ul>
                            <li>Visualizar el estado de todos los sensores (activos, inactivos o con errores).</li>
                            <li>Consultar históricos de mapas de contaminación.</li>
                            <li>Gestionar incidencias enviadas por los usuarios.</li>
                            <li>Responder incidencias y marcarlas como resueltas.</li>
                            <li>Acceder a información global del sistema desde el portal web.</li>
                        </ul>
                    </div>
                </details>

                <!-- 6. ¿Qué requisitos necesita la aplicación móvil? -->
                <details class="faq-item">
                    <summary>6. ¿Qué requisitos necesita la aplicación móvil?</summary>
                    <div class="faq-content">
                        <p>Para utilizar la aplicación móvil ATMOS es necesario:</p>
                        <ul>
                            <li>Un dispositivo con Android 8.0 o superior.</li>
                            <li>Bluetooth activado.</li>
                            <li>GPS activado.</li>
                            <li>Conexión a Internet.</li>
                        </ul>
                    </div>
                </details>

                <!-- 7. ¿Cómo se accede al portal web? -->
                <details class="faq-item">
                    <summary>7. ¿Cómo se accede al portal web?</summary>
                    <div class="faq-content">
                        <p>El acceso al portal web se realiza desde un navegador moderno (Chrome, Firefox, Edge o
                            Safari) utilizando la siguiente dirección oficial:</p>
                        <p><a href="https://nagufor.upv.edu.es/cliente/index.php"
                                target="_blank">https://nagufor.upv.edu.es/cliente/index.php</a></p>
                        <p>Las credenciales de acceso son las mismas que se utilizan en la aplicación móvil.</p>
                    </div>
                </details>

                <!-- 8. ¿Cómo funciona el registro y el inicio de sesión? -->
                <details class="faq-item">
                    <summary>8. ¿Cómo funciona el registro y el inicio de sesión?</summary>
                    <div class="faq-content">
                        <p><strong>Registro:</strong> el usuario debe introducir un correo electrónico válido y una
                            contraseña. Posteriormente debe activar la cuenta mediante un correo de confirmación.</p>
                        <p><strong>Inicio de sesión:</strong> se accede introduciendo las credenciales registradas.</p>
                        <p><strong>Recuperación de contraseña:</strong> disponible a través de correo electrónico en
                            caso de olvido.</p>
                    </div>
                </details>

                <!-- 9. ¿Qué información muestra el mapa de contaminación? -->
                <details class="faq-item">
                    <summary>9. ¿Qué información muestra el mapa de contaminación?</summary>
                    <div class="faq-content">
                        <p>El mapa de contaminación muestra:</p>
                        <ul>
                            <li>Mediciones recientes de los sensores.</li>
                            <li>Colores que representan los niveles de contaminación.</li>
                            <li>Opciones de filtrado por tipo de contaminante.</li>
                        </ul>
                    </div>
                </details>

                <!-- 10. ¿Cómo se reporta una incidencia? -->
                <details class="faq-item">
                    <summary>10. ¿Cómo se reporta una incidencia?</summary>
                    <div class="faq-content">
                        <p>Para reportar una incidencia:</p>
                        <ol>
                            <li>Acceder al apartado Incidencias.</li>
                            <li>Crear una nueva incidencia vinculada al sensor activo.</li>
                            <li>Describir el problema detectado.</li>
                        </ol>
                        <p>El usuario puede consultar posteriormente el estado y la resolución de la incidencia.</p>
                    </div>
                </details>

                <!-- 11. ¿Qué funcionalidades ofrece el portal web? -->
                <details class="faq-item">
                    <summary>11. ¿Qué funcionalidades ofrece el portal web?</summary>
                    <div class="faq-content">
                        <p>Desde el portal web se puede:</p>
                        <ul>
                            <li>Consultar mapas globales de contaminación.</li>
                            <li>Visualizar datos históricos en formato tabla.</li>
                            <li>Editar el perfil de usuario.</li>
                            <li>Consultar y seguir incidencias.</li>
                            <li>Vincular o desvincular sensores.</li>
                            <li>Acceder al manual de usuario.</li>
                        </ul>
                    </div>
                </details>

                <!-- 12. ¿Es necesario emparejar manualmente los sensores? -->
                <details class="faq-item">
                    <summary>12. ¿Es necesario emparejar manualmente los sensores?</summary>
                    <div class="faq-content">
                        <p>No. Los sensores emiten los datos mediante Bluetooth BLE y no requieren emparejamiento
                            manual. Únicamente deben estar encendidos para poder transmitir información.</p>
                    </div>
                </details>

                <!-- 13. ¿Qué medidas de seguridad se recomiendan? -->
                <details class="faq-item">
                    <summary>13. ¿Qué medidas de seguridad se recomiendan?</summary>
                    <div class="faq-content">
                        <p>Se recomienda:</p>
                        <ul>
                            <li>Utilizar contraseñas seguras.</li>
                            <li>No compartir credenciales de acceso.</li>
                            <li>Conceder solo los permisos necesarios.</li>
                            <li>Cerrar sesión en dispositivos compartidos.</li>
                        </ul>
                    </div>
                </details>

                <!-- 14. ¿Cómo se obtiene soporte técnico? -->
                <details class="faq-item">
                    <summary>14. ¿Cómo se obtiene soporte técnico?</summary>
                    <div class="faq-content">
                        <p>Para incidencias técnicas se puede:</p>
                        <ul>
                            <li>Utilizar el sistema de incidencias integrado en la aplicación.</li>
                            <li>Contactar por correo electrónico en: <a
                                    href="mailto:soporte.atmos@gmail.com">soporte.atmos@gmail.com</a></li>
                        </ul>
                    </div>
                </details>

            </div>
        </section>

        <section id="compra-sensor" class="section-cta">
            <div class="wrap">
                <h2 class="cta-title">Comienza a monitorizar el aire hoy con la compra de tu sensor</h2>
                <p class="cta-sub">
                    Únete a cientos de personas que ya están contribuyendo a un futuro más saludable
                </p>

                <div class="cta-actions">
                    <a href="solucion.php" class="btn btn-secondary btn-lg">Compra tu sensor</a>
                </div>
            </div>
        </section>

    </main>

    <?php
    /**
     * @brief Inclusión del footer público.
     *
     * Contiene enlaces legales, información de contacto
     * y créditos del proyecto.
     */
    include __DIR__ . '/partials/footer.php';
    ?>

    <!--
/**
 * @section Scroll Suave
 * @brief Implementa desplazamiento suave entre anclas internas.
 */
-->
    <script>
        document.addEventListener('click', e => {
            const a = e.target.closest('a[href^="#"]');
            if (!a) return;
            const el = document.querySelector(a.getAttribute('href'));
            if (!el) return;
            e.preventDefault();
            el.scrollIntoView({ behavior: 'smooth', block: 'start' });
        });
    </script>

</body>

</html>