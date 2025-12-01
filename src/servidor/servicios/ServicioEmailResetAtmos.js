/**
 * @file ServicioEmailResetAtmos.js
 * @brief Servicio encargado de enviar correos personalizados de restablecimiento
 *        de contraseña para Atmos, sustituyendo el email genérico de Firebase.
 * @author Alan Guevara Martínez
 * @date 01/12/2025
 */

const nodemailer = require("nodemailer");

/**
 * @brief Crea un transportista SMTP basado en variables de entorno.
 */
function crearTransportista() {
    return nodemailer.createTransport({
        host: process.env.SMTP_HOST,
        port: Number(process.env.SMTP_PORT || 587),
        secure: false,
        auth: {
            user: process.env.SMTP_USER,
            pass: process.env.SMTP_PASS
        }
    });
}

const transporteCorreo = crearTransportista();

/**
 * @brief Genera el HTML del correo de restablecimiento Atmos.
 *
 * @param nombreUsuario Nombre del usuario.
 * @param enlace URL segura generada por Firebase Admin.
 */
function generarHtmlReset(nombreUsuario, enlace) {
    const nombre = nombreUsuario || "usuario/a";

    return `
    <!DOCTYPE html>
    <html lang="es">
    <body style="font-family: Arial; background:#f3f4f6; padding:25px;">
        <div style="max-width:480px;margin:auto;background:white;
            border-radius:14px;padding:30px;box-shadow:0 6px 20px rgba(0,0,0,0.1);">

            <div style="text-align:center;">
                <img src="https://nagufor.upv.edu.es/cliente/img/logoAtmosCorreo.png"
                     style="width:120px;margin-bottom:10px;">
                <h2 style="margin:0;color:#111827;">
                    Restablece tu contraseña
                </h2>
            </div>

            <p style="color:#4b5563;font-size:15px;">
                Hola <strong>${nombre}</strong>,
                has solicitado cambiar la contraseña de tu cuenta en Atmos.
            </p>

            <p style="color:#4b5563;">
                Haz clic en el siguiente botón para completar el proceso:
            </p>

            <div style="text-align:center;margin:20px 0;">
                <a href="${enlace}"
                   style="padding:12px 25px;background:#10B981;color:white;text-decoration:none;
                   border-radius:30px;font-weight:bold;">
                    Cambiar contraseña
                </a>
            </div>

            <p style="font-size:12px;color:#6b7280;">
                Si el botón no funciona, copia y pega el siguiente enlace:
                <br>
                <a href="${enlace}" style="color:#10B981;">
                    ${enlace}
                </a>
            </p>

            <p style="font-size:11px;color:#9ca3af;">
                Si no solicitaste este cambio, puedes ignorar el correo.
            </p>

        </div>
    </body>
    </html>
    `;
}

/**
 * @brief Envía el correo personalizado.
 */
async function enviarCorreoReset(destinatario, nombreUsuario, enlace) {
    const html = generarHtmlReset(nombreUsuario, enlace);

    return transporteCorreo.sendMail({
        from: {
            name: "Atmos",
            address: process.env.SMTP_FROM || process.env.SMTP_USER
        },
        to: destinatario,
        subject: "Restablece tu contraseña - Atmos",
        html
    });
}

module.exports = { enviarCorreoReset };
