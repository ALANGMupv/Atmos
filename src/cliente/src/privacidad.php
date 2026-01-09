<!--
/**
 * @file politica_privacidad.html
 * @brief Página de Política de Privacidad de la plataforma ATMOS.
 *
 * Este documento describe cómo ATMOS recopila, utiliza, almacena y protege
 * los datos personales de los usuarios tanto en la aplicación móvil como
 * en la web informativa.
 *
 * Cumple con:
 *  - Reglamento General de Protección de Datos (GDPR)
 *  - Principios de transparencia, minimización y seguridad
 *
 * La página es de solo lectura y no recopila datos directamente.
 *
 * @author Equipo ATMOS
 * @version 1.0
 * @date 2025-11-29
 */
-->
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Política de Privacidad – Atmos</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <!--
    /**
     * @brief Tipografía principal usada en la página.
     */
    -->
    <link href="https://fonts.googleapis.com/css2?family=Open+Sans:wght@300;400;600;700&display=swap" rel="stylesheet">

    <!--
    /**
     * @brief Hoja de estilos específica para la página de privacidad.
     */
    -->
    <link rel="stylesheet" href="css/privacidad.css">

    <!--
    /**
     * @brief Función para cerrar la página y volver a la vista anterior.
     *
     * Utiliza el historial del navegador (`window.history.back()`).
     */
    -->
    <script>
        function cerrar() {
            window.history.back();
        }
    </script>
</head>
<body>

<!--
/**
 * @section Barra superior
 * @brief Cabecera fija con título y botón de cierre.
 */
-->
<div class="top-bar">
    <strong>Política de Privacidad</strong>
    <img src="icons/cerrar.svg" class="close-btn" onclick="cerrar()" alt="Cerrar">
</div>

<!--
/**
 * @section Contenido principal
 * @brief Texto legal completo de la Política de Privacidad.
 */
-->
<h1>Política de Privacidad de Atmos</h1>
<p><strong>Última actualización: 29 de noviembre de 2025</strong></p>

<p>Esta Política de Privacidad explica cómo Atmos recopila, utiliza y protege los datos personales de los usuarios cuando utilizan la aplicación móvil Atmos (“la Aplicación”) y cuando visitan la página web informativa de Atmos (“la Web”).</p>

<p>Al utilizar la Aplicación o la Web, usted acepta el tratamiento de sus datos según lo establecido en esta Política de Privacidad.</p>

<!--
/**
 * @section Definiciones
 * @brief Términos clave utilizados en la Política de Privacidad.
 */
-->
<h2>1. Definiciones</h2>
<p><strong>Cuenta:</strong> perfil único creado para acceder a la Aplicación.<br>
<strong>Aplicación:</strong> Atmos.<br>
<strong>Web:</strong> Sitio web informativo de Atmos.<br>
<strong>Empresa:</strong> Atmos.<br>
<strong>Datos Personales:</strong> información que puede identificar a una persona física.<br>
<strong>Datos de Uso:</strong> datos técnicos recopilados automáticamente.<br>
<strong>Proveedor de Servicios:</strong> terceros que procesan datos en nombre de la Empresa.<br>
<strong>Usted:</strong> persona que usa la Aplicación o la Web.</p>

<!--
/**
 * @section Datos recopilados
 * @brief Tipos de información recogida por ATMOS.
 */
-->
<h2>2. Datos que Recopilamos</h2>

<h3>2.1. Datos proporcionados por el usuario</h3>
<p>Para crear una cuenta en Atmos es necesario proporcionar:<br>
• Nombre<br>
• Apellidos<br>
• Correo electrónico</p>

<h3>2.2. Datos técnicos y de uso</h3>
<p>Al utilizar la Aplicación se recopilan automáticamente:<br>
• Modelo del dispositivo<br>
• Sistema operativo<br>
• Versión de la App<br>
• Dirección IP<br>
• Identificadores únicos<br>
• Registros de funcionamiento y errores</p>

<h3>2.3. Datos de ubicación</h3>
<p>La Aplicación utiliza la ubicación del dispositivo exclusivamente para:<br>
• Asociar mediciones ambientales a un punto geográfico<br>
• Mostrar información contextual dentro de la Aplicación</p>
<p>La ubicación solo se recoge con permiso explícito del usuario y puede desactivarse en cualquier momento desde los ajustes del dispositivo.</p>

<h3>2.4. Acceso a la cámara</h3>
<p>La cámara del dispositivo se utiliza únicamente para:<br>
• Escanear códigos QR<br>
• Vincular sensores o dispositivos compatibles con Atmos</p>
<p>No se toman fotografías ni se graban vídeos fuera de este uso.</p>

<h3>2.5. Datos generados dentro de la Aplicación</h3>
<p>Atmos puede almacenar:<br>
• Lecturas ambientales enviadas por sensores<br>
• Información procedente de dispositivos vinculados<br>
• Preferencias y configuraciones del usuario</p>

<!--
/**
 * @section Servicios externos
 * @brief Proveedores de terceros utilizados por ATMOS.
 */
-->
<h2>3. Servicios de Terceros Utilizados</h2>

<h3>3.1. Firebase Authentication</h3>
<p>Utilizado para la creación y gestión de cuentas, recuperación de contraseñas y verificación de correos electrónicos.</p>

<h3>3.2. Firestore / Realtime Database</h3>
<p>Utilizado para almacenar datos del usuario, configuraciones internas y lecturas generadas por dispositivos Atmos.</p>

<h3>3.3. Firebase Analytics</h3>
<p>La versión actual de Atmos no utiliza Firebase Analytics. Si se habilita en el futuro, esta Política será actualizada.</p>

<!--
/**
 * @section Uso de datos
 * @brief Finalidades del tratamiento de la información.
 */
-->
<h2>4. Uso de los Datos</h2>
<p>Los datos recopilados se utilizan para:<br>
• Ofrecer y mantener la Aplicación<br>
• Asociar mediciones ambientales a la ubicación del usuario<br>
• Gestionar cuentas y autenticación<br>
• Garantizar la seguridad y estabilidad del Servicio<br>
• Analizar fallos y mejorar funciones<br>
• Responder a solicitudes de soporte<br>
• Cumplir obligaciones legales</p>
<p>Atmos no vende datos personales ni los utiliza con fines publicitarios.</p>

<!--
/**
 * @section Derechos del usuario
 * @brief Derechos reconocidos por la normativa de protección de datos.
 */
-->
<h2>8. Derechos del Usuario</h2>
<p>Usted tiene derecho a:<br>
• Acceder a sus datos personales<br>
• Rectificar información incorrecta<br>
• Solicitar la eliminación de sus datos<br>
• Limitar el tratamiento<br>
• Oponerse cuando la ley lo permita</p>
<p>Para ejercer estos derechos puede contactar en: <strong>soporte.atmos@gmail.com</strong></p>

<!--
/**
 * @section Contacto
 * @brief Información de contacto para cuestiones de privacidad.
 */
-->
<h2>13. Contacto</h2>
<p>Para dudas relacionadas con esta Política de Privacidad puede escribir a:<br>
<strong>soporte.atmos@gmail.com</strong></p>

</body>
</html>
