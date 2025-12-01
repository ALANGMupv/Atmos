/**
 * @file servicioEmailAtmos.js
 * @brief Servicio encargado de enviar correos electrónicos desde el backend.
 *
 * Autor: Alan Guevara Martínez  
 * Fecha: 01/12/2025
 *
 * Responsabilidades:
 *  - Configurar el transporte SMTP (servidor de correo).
 *  - Generar el HTML de los correos de Atmos.
 *  - Enviar el correo de verificación de cuenta.
 */

const nodemailer = require("nodemailer");

/**
 * @brief Crea y devuelve un transportista SMTP configurado con nodemailer.
 *
 * @details
 *  Crea y devuelve un "transporter" de nodemailer configurado con tu servidor SMTP.
 *
 * @note
 *  - Ideal: configurar host/puerto/usuario/pass vía variables de entorno.  
 *  - En Plesk puedes usar el SMTP del dominio o el de la universidad.
 *
 * @return {object} Instancia configurada de nodemailer.createTransport().
 */
function crearTransportistaCorreo() {
    return nodemailer.createTransport({
        host: process.env.SMTP_HOST,               ///< p.ej. "smtp.upv.es" o "smtp.tudominio.com"
        port: Number(process.env.SMTP_PORT || 587),
        secure: false,                             ///< true si usas 465
        auth: {
            user: process.env.SMTP_USER,           ///< correo emisor (no-reply@tudominio.com)
            pass: process.env.SMTP_PASS            ///< contraseña o app password
        }
    });
}

// Instancia única del transportista (reutilizable)
const transportistaCorreo = crearTransportistaCorreo();

/**
 * @brief Genera el HTML completo del correo de verificación de Atmos.
 *
 * @param nombreUsuario Nombre del usuario que recibirá el correo.
 * @param enlaceVerificacion Enlace único para verificar la cuenta.
 *
 * @return {string} HTML generado para el email.
 */
function generarHtmlVerificacionAtmos(nombreUsuario, enlaceVerificacion) {

    const nombreSeguro = nombreUsuario || "usuario/a";

    return `
    <!DOCTYPE html>
    <html lang="es">
    <head>
        <meta charset="UTF-8" />
        <title>Verifica tu cuenta - Atmos</title>
    </head>
    <body style="margin:0; padding:0; background:#f3f4f6; font-family:Arial,Helvetica,sans-serif;">
        <div style="width:100%; padding:30px 0; background:#f3f4f6;">
            <div style="
                max-width:480px;
                margin:0 auto;
                background:#ffffff;
                border-radius:16px;
                padding:30px 25px 35px;
                box-shadow:0 10px 25px rgba(0,0,0,0.05);
            ">

                <!-- LOGO / CABECERA -->
                <div style="text-align:center; margin-bottom:20px;">
                    <img src="https://nagufor.upv.edu.es/cliente/img/logoAtmosCorreo.png"
                    alt="Logo Atmos"
                    style="width:120px; margin-bottom:10px;" />
                    <h1 style="margin:0; font-size:22px; color:#111827;">
                        Bienvenido/a a <span style="color:#10B981;">Atmos</span>
                    </h1>
                </div>

                <!-- CONTENIDO PRINCIPAL -->
                <p style="font-size:14px; color:#374151; margin-top:20px;">
                    Hola <strong>${nombreSeguro}</strong>,
                </p>

                <p style="font-size:14px; color:#4b5563; line-height:1.6;">
                    Gracias por registrarte en <strong>Atmos</strong>.
                    Antes de empezar a usar la aplicación, necesitamos que confirmes
                    tu dirección de correo electrónico.
                </p>

                <p style="font-size:14px; color:#4b5563; line-height:1.6;">
                    Haz clic en el siguiente botón para verificar tu cuenta:
                </p>

                <!-- BOTÓN -->
                <div style="text-align:center; margin:26px 0;">
                    <a href="${enlaceVerificacion}"
                       style="
                           display:inline-block;
                           padding:12px 24px;
                           border-radius:999px;
                           background:#10B981;
                           color:#ffffff;
                           text-decoration:none;
                           font-weight:bold;
                           font-size:15px;
                       ">
                        Verificar correo
                    </a>
                </div>

                <!-- ENLACE DE TEXTO ALTERNATIVO -->
                <p style="font-size:12px; color:#6b7280; line-height:1.6;">
                    Si el botón no funciona, copia y pega el siguiente enlace en tu navegador:
                    <br />
                    <a href="${enlaceVerificacion}" style="color:#10B981; word-break:break-all;">
                        ${enlaceVerificacion}
                    </a>
                </p>

                <!-- PIE -->
                <p style="font-size:11px; color:#9ca3af; margin-top:24px;">
                    Si tú no has creado una cuenta en Atmos, puedes ignorar este correo.
                </p>
            </div>
        </div>
    </body>
    </html>
    `;
}

/**
 * @brief Envía el correo de verificación de Atmos a un destinatario específico.
 *
 * @param destinatario Email de destino.
 * @param nombreUsuario Nombre del usuario que recibirá el correo.
 * @param enlaceVerificacion URL única para verificar la cuenta.
 *
 * @return {Promise<void>} Promesa que se resuelve cuando el correo es enviado.
 */
async function enviarCorreoVerificacionAtmos(destinatario, nombreUsuario, enlaceVerificacion) {

    console.log("=== INICIO ENVÍO EMAIL ATMOS ===");
    console.log("SMTP_HOST:", process.env.SMTP_HOST);
    console.log("SMTP_USER:", process.env.SMTP_USER);
    console.log("ENV password length:", process.env.SMTP_PASS.length);
    console.log("Enviando a:", destinatario);
    console.log("Enlace:", enlaceVerificacion);

    const html = generarHtmlVerificacionAtmos(nombreUsuario, enlaceVerificacion);

    try {
        const info = await transportistaCorreo.sendMail({
            from: `"Atmos" <${process.env.SMTP_FROM || process.env.SMTP_USER}>`,
            to: destinatario,
            subject: "Verifica tu cuenta de Atmos",
            html
        });

        console.log("Email ENVIADO correctamente:", info.messageId);

    } catch (error) {
        console.error("ERROR enviando email ATMOS:");
        console.error(error);
    }

    console.log("=== FIN ENVÍO EMAIL ATMOS ===");
}

module.exports = {
    enviarCorreoVerificacionAtmos
};
