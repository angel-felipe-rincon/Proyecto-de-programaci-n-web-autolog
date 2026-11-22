$(function () {
    sessionStorage.clear();

    App.iniciar(function () {
        abrirPestanaSegunEnlace();
        $(window).on('hashchange', abrirPestanaSegunEnlace);

        $('#form-login').on('submit', function (e) {
            e.preventDefault();
            App.api('POST', '/api/auth/login', { correo: $('#l-correo').val(), clave: $('#l-clave').val() })
                .done(function (d) { App.guardarSesion(d); App.irAlPanel(d.rol); })
                .fail(function (xhr) {
                    var clave = xhr.status === 401 ? 'auth.invalidas' : xhr.status === 403 ? 'auth.cuentaNoActiva' : null;
                    App.aviso('#avisos', 'danger', clave ? App.t(clave) : App.mensajeError(xhr));
                });
        });
        registro('#form-cliente', '/api/auth/registro-cliente', 'auth.clienteCreado');
        registro('#form-taller', '/api/auth/registro-taller', 'auth.tallerCreado');
    });

    function abrirPestanaSegunEnlace() {
        var id = { '#cliente': 'btn-cliente', '#taller': 'btn-taller', '#ingresar': 'btn-login' }[location.hash];
        if (id) { mostrarPestana(id); }
    }
    function mostrarPestana(id) {
        bootstrap.Tab.getOrCreateInstance(document.getElementById(id)).show();
        $('#avisos').empty();
    }

    $('[data-abrir-tab]').on('click', function (e) {
        e.preventDefault();
        mostrarPestana($(this).attr('data-abrir-tab'));
    });

    $('[data-ver-clave]').on('click', function () {
        var $input = $('#' + $(this).attr('data-ver-clave'));
        var visible = $input.attr('type') === 'text';
        $input.attr('type', visible ? 'password' : 'text');
        $(this).text(App.t(visible ? 'ing.mostrar' : 'ing.ocultar')).attr('aria-pressed', !visible);
    });

    function registro(form, url, mensaje) {
        var $f = $(form);
        $f.on('submit', function (e) {
            e.preventDefault();
            var correo = $f.find('[name=correo]').val();
            App.api('POST', url, {
                nombre: $f.find('[name=nombre]').val(),
                correo: correo,
                clave: $f.find('[name=clave]').val()
            }).done(function () {
                $f[0].reset();
                mostrarPestana('btn-login');
                $('#l-correo').val(correo);
                $('#l-clave').trigger('focus');
                App.aviso('#avisos', 'success', App.t(mensaje));
            }).fail(function (xhr) { App.aviso('#avisos', 'danger', App.mensajeError(xhr)); });
        });
    }
});