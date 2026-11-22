$(function () {
    App.iniciar(function () {
        var p = new URLSearchParams(location.hash.substring(1));
        var tk = p.get('token');
        if (tk) {
            var d = App.datosDelToken(tk);
            App.guardarSesion({ token: tk, rol: d.rol, nombre: d.nombre });
            history.replaceState(null, '', location.pathname);
            App.irAlPanel(d.rol);
        } else {
            $('#cargando').addClass('d-none');
            $('#mensaje').removeClass('d-none').text(App.t('google.error.' + (p.get('error') || 'google')));
            $('#volver').removeClass('d-none');
        }
    });
});