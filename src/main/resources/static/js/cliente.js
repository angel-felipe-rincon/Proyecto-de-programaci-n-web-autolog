$(function () {
    if (!App.exigirRol('CLIENTE')) { return; }
    var modal = new bootstrap.Modal(document.getElementById('modal-vehiculo'));
    var actual = null;

    App.iniciar(function () {
        $('#saludo').text(App.t('comun.hola', App.nombre()));
        $('#salir').on('click', App.cerrarSesion);
        cargarAlertas();
        cargarVehiculos();
    });

    function error(xhr) { App.aviso('#avisos', 'danger', App.mensajeError(xhr)); }

    function vacio(icono, clave) {
        return '<div class="estado-vacio"><i class="bi ' + icono + '" aria-hidden="true"></i>' +
            App.esc(App.t(clave)) + '</div>';
    }

    function cargarAlertas() {
        App.api('GET', '/api/cliente/alertas').done(function (lista) {
            $('#lista-alertas').html(lista.length ? lista.map(function (a) {
                return '<div class="alert alert-warning d-flex gap-2 mb-2" role="status">' +
                    '<i class="bi bi-exclamation-triangle-fill" aria-hidden="true"></i><div>' +
                    App.esc(App.t('cli.alerta', a.servicio, a.placa, App.num(a.proximoKm))) + '</div></div>';
            }).join('') : '<p class="text-success mb-0"><i class="bi bi-check-circle" aria-hidden="true"></i> ' +
                App.esc(App.t('cli.sinAlertas')) + '</p>');
        });
    }

    function cargarVehiculos() {
        App.api('GET', '/api/cliente/vehiculos').done(function (lista) {
            $('#lista-vehiculos').html(lista.length ? lista.map(function (v) {
                var icono = v.tipo === 'MOTO' ? 'bi-scooter' : 'bi-car-front';
                var elegido = actual && actual.id === v.id ? ' border-primary border-2' : '';
                return '<div class="col-md-6 col-lg-4"><div class="card h-100' + elegido + '"><div class="card-body">' +
                    '<div class="d-flex align-items-center gap-2 mb-2"><span class="icono-circulo" style="width:2.5rem;height:2.5rem;font-size:1.2rem">' +
                    '<i class="bi ' + icono + '" aria-hidden="true"></i></span>' +
                    '<h3 class="h5 mb-0">' + App.esc(v.placa) + '</h3></div>' +
                    '<p class="mb-3">' + App.esc(v.marca + ' ' + v.modelo + ' (' + v.anio + ')') + '<br>' +
                    '<span class="text-muted small">' + App.esc(App.t('tipo.' + v.tipo) + ' · ' + App.num(v.kilometraje) + ' km') + '</span></p>' +
                    '<button type="button" class="btn btn-outline-primary btn-sm" data-abrir="' + v.id + '">' +
                    '<i class="bi bi-folder2-open" aria-hidden="true"></i> ' + App.esc(App.t('cli.abrir')) + '</button></div></div></div>';
            }).join('') : '<div class="col-12">' + vacio('bi-car-front', 'cli.sinVehiculos') + '</div>');
        });
    }
    $('#lista-vehiculos').on('click', '[data-abrir]', function () { abrirFicha($(this).attr('data-abrir')); });

    function abrirFicha(id) {
        $.when(App.api('GET', '/api/cliente/vehiculos/' + id),
               App.api('GET', '/api/cliente/talleres'),
               App.api('GET', '/api/cliente/vehiculos/' + id + '/mantenimientos'))
        .done(function (v, talleres, historial) {
            actual = v[0];
            $('#t-ficha').text(actual.placa);
            $('#ficha-datos').text(actual.marca + ' ' + actual.modelo + ' (' + actual.anio + ') · ' +
                App.t('tipo.' + actual.tipo) + ' · ' + App.num(actual.kilometraje) + ' km');
            $('#km-nuevo').val(actual.kilometraje).attr('min', actual.kilometraje);

            var $t = $('#lista-talleres').empty();
            if (!talleres[0].length) { $t.html('<p class="small text-muted mb-0">' + App.esc(App.t('cli.sinTalleres')) + '</p>'); }
            talleres[0].forEach(function (t) {
                var marcado = actual.talleresAutorizados.indexOf(t.id) >= 0;
                $t.append('<div class="form-check"><input class="form-check-input" type="checkbox" id="taller-' + t.id +
                    '" value="' + t.id + '"' + (marcado ? ' checked' : '') + '>' +
                    '<label class="form-check-label" for="taller-' + t.id + '">' + App.esc(t.nombre) + '</label></div>');
            });
            $('#historial').html(App.filasHistorial(historial[0], false));
            $('#ficha').removeClass('d-none');
            $('#t-ficha').trigger('focus');
            cargarVehiculos();
        }).fail(error);
    }

    $('#btn-cerrar-ficha').on('click', function () {
        $('#ficha').addClass('d-none');
        actual = null;
        cargarVehiculos();
        $('#t-vehiculos').closest('section')[0].scrollIntoView({ behavior: 'smooth' });
    });

    $('#form-km').on('submit', function (e) {
        e.preventDefault();
        App.api('PATCH', '/api/cliente/vehiculos/' + actual.id + '/kilometraje',
                { kilometraje: parseInt($('#km-nuevo').val(), 10) })
            .done(function () {
                App.aviso('#avisos', 'success', App.t('cli.kmGuardado'));
                abrirFicha(actual.id); cargarAlertas();
            }).fail(error);
    });

    $('#form-talleres').on('submit', function (e) {
        e.preventDefault();
        var ids = $('#lista-talleres input:checked').map(function () { return parseInt(this.value, 10); }).get();
        App.api('PUT', '/api/cliente/vehiculos/' + actual.id + '/talleres', { ids: ids })
            .done(function () { App.aviso('#avisos', 'success', App.t('cli.talleresGuardados')); }).fail(error);
    });

    $('#btn-nuevo').on('click', function () {
        $('#avisos-modal').empty();
        $('#form-vehiculo')[0].reset();
        modal.show();
    });

    $('#form-vehiculo').on('submit', function (e) {
        e.preventDefault();
        App.api('POST', '/api/cliente/vehiculos', {
            placa: $('#v-placa').val(), marca: $('#v-marca').val(),
            modelo: $('#v-modelo').val(), anio: parseInt($('#v-anio').val(), 10),
            tipo: $('#v-tipo').val(), kilometraje: parseInt($('#v-km').val(), 10)
        }).done(function () {
            modal.hide();
            App.aviso('#avisos', 'success', App.t('cli.vehiculoCreado'));
            cargarVehiculos();
        }).fail(function (xhr) { App.aviso('#avisos-modal', 'danger', App.mensajeError(xhr)); });
    });

    $('#btn-eliminar').on('click', function () {
        if (!window.confirm(App.t('cli.confirmarEliminar', actual.placa))) { return; }
        App.api('DELETE', '/api/cliente/vehiculos/' + actual.id).done(function () {
            $('#ficha').addClass('d-none');
            actual = null;
            App.aviso('#avisos', 'success', App.t('cli.vehiculoEliminado'));
            cargarVehiculos(); cargarAlertas();
        }).fail(error);
    });

    $('#btn-pdf').on('click', function () {
        App.descargar('/api/cliente/vehiculos/' + actual.id + '/historial.pdf?idioma=' + App.idioma(),
                      'historial-' + actual.placa + '.pdf')
            .catch(function () { App.aviso('#avisos', 'danger', App.t('err.generico')); });
    });
});