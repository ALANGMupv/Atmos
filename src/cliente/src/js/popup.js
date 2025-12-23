document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('[data-popup]').forEach(btn => {
    btn.addEventListener('click', () => {
      const id = btn.dataset.popup;
      const popup = document.getElementById(id);
      if (popup) popup.classList.add('activo');
    });
  });

  document.querySelectorAll('.cerrar-popup').forEach(btn => {
    btn.addEventListener('click', () => {
      btn.closest('.popup-info-container').classList.remove('activo');
    });
  });
});
