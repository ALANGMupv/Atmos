/**
 * @file popups.js
 * @brief Gestión genérica de popups informativos mediante atributos data.
 *
 * Este script se encarga de:
 *  - Abrir popups al pulsar botones con el atributo `data-popup`.
 *  - Cerrar popups al pulsar botones con la clase `cerrar-popup`.
 *  - Activar y desactivar los popups mediante la clase CSS `activo`.
 *
 * El sistema es reutilizable y desacoplado del contenido concreto de cada popup.
 */

document.addEventListener('DOMContentLoaded', () => {

  /**
   * @brief Asocia el evento de apertura a todos los botones con data-popup.
   *
   * El valor del atributo `data-popup` debe coincidir con el `id`
   * del contenedor del popup que se desea mostrar.
   */
  document.querySelectorAll('[data-popup]').forEach(btn => {
    btn.addEventListener('click', () => {
      const id = btn.dataset.popup;
      const popup = document.getElementById(id);

      if (popup) {
        popup.classList.add('activo');
      }
    });
  });

  /**
   * @brief Asocia el evento de cierre a los botones de cierre del popup.
   *
   * Se busca el contenedor padre con la clase `popup-info-container`
   * y se elimina la clase `activo` para ocultarlo.
   */
  document.querySelectorAll('.cerrar-popup').forEach(btn => {
    btn.addEventListener('click', () => {
      btn.closest('.popup-info-container').classList.remove('activo');
    });
  });

});
