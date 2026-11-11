/* =========================================================
   Inventario de Activos de TI - Colegio Ayacucho
   Comportamiento general de la interfaz
   ========================================================= */
(function () {
    'use strict';

    // 1) Confirmación antes de enviar formularios marcados con data-confirm
    //    (se usa en eliminaciones de activos, tipos, ubicaciones y usuarios).
    document.addEventListener('submit', function (evento) {
        var formulario = evento.target.closest('form[data-confirm]');
        if (formulario) {
            var mensaje = formulario.getAttribute('data-confirm') || '¿Confirma la operación?';
            if (!window.confirm(mensaje)) {
                evento.preventDefault();
                evento.stopPropagation();
            }
        }
    });

    // 2) Cierre automático de los mensajes flash a los 6 segundos
    window.setTimeout(function () {
        document.querySelectorAll('.alert-dismissible').forEach(function (alerta) {
            if (window.bootstrap && window.bootstrap.Alert) {
                window.bootstrap.Alert.getOrCreateInstance(alerta).close();
            }
        });
    }, 6000);

    // 3) Mantener el foco en el primer campo con error de validación
    var primerInvalido = document.querySelector('.is-invalid');
    if (primerInvalido) {
        primerInvalido.focus();
    }
})();
