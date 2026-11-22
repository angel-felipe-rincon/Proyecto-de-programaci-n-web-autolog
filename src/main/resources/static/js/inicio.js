$(function () {
    $('.espacio-imagen img').each(function () {
        var img = this;
        function sinImagen() { $(img).closest('.espacio-imagen').addClass('sin-imagen'); }
        if (img.complete && img.naturalWidth === 0) { sinImagen(); }
        $(img).on('error', sinImagen);
    });

    App.iniciar(function () {
        var rol = App.sesionActiva();
        if (rol) {
            $('#botones-invitado').addClass('d-none').removeClass('d-flex');
            $('#botones-sesion').removeClass('d-none').addClass('d-flex');
            $('#ir-panel').on('click', function () { App.irAlPanel(rol); });
            $('#salir').on('click', App.cerrarSesion);
        }
    });
});