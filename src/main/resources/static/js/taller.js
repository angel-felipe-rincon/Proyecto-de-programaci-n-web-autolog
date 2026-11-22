$(function () {
    if (!App.exigirRol('TALLER')) { return; }
    var modal = new bootstrap.Modal(document.getElementById('modal-edicion'));
    var vehiculo = null, historial = [];

    App.iniciar(function () {
        $('#saludo').text(App.t('comun.hola', App.nombre()));
        $('#salir').on('click', App.cerrarSesion);
    });

    function error(xhr) { App.aviso('#avisos', 'danger', App.mensajeError(xhr)); }

    $('#form-buscar').on('submit', function (e) {
        e.preventDefault();
        $('#avisos').empty();
        buscar(true);
    });

    function buscar(enfocar) {
        App.api('GET', '/api/taller/vehiculos/buscar?placa=' + encodeURIComponent($('#placa').val().trim()))
            .done(function (v) { vehiculo = v; mostrar(enfocar); })
            .fail(function (xhr) {
                $('#resultado').addClass('d-none');
                $('#sin-resultado').removeClass('d-none');
                error(xhr);
            });
    }

    function mostrar(enfocar) {
        $('#sin-resultado').addClass('d-none');
        $('#resultado').removeClass('d-none');
        if (enfocar) { $('#t-vehiculo').trigger('focus'); }
        $('#t-vehiculo').text(vehiculo.placa);
        $('#datos-vehiculo').text(vehiculo.marca + ' ' + vehiculo.modelo + ' (' + vehiculo.anio + ') · ' +
            App.t('tipo.' + vehiculo.tipo) + ' · ' + App.num(vehiculo.kilometraje) + ' km');
        $('#m-km').val(vehiculo.kilometraje).attr('min', vehiculo.kilometraje);
        App.api('GET', '/api/taller/vehiculos/' + vehiculo.id + '/servicios').done(function (lista) {
            $('#m-servicio').html(lista.map(function (s) {
                return '<option value="' + s.id + '">' + App.esc(s.nombre) + '</option>';
            }).join(''));
        });
        cargarHistorial();
    }

    function cargarHistorial() {
        App.api('GET', '/api/taller/vehiculos/' + vehiculo.id + '/mantenimientos').done(function (lista) {
            historial = lista;
            $('#historial').html(App.filasHistorial(lista, true));
        });
    }

    $('#form-mantenimiento').on('submit', function (e) {
        e.preventDefault();
        App.api('POST', '/api/taller/vehiculos/' + vehiculo.id + '/mantenimientos', {
            tipoServicioId: parseInt($('#m-servicio').val(), 10),
            kilometraje: parseInt($('#m-km').val(), 10),
            costo: parseFloat($('#m-costo').val()),
            observaciones: $('#m-obs').val().trim() || null
        }).done(function () {
            App.aviso('#avisos', 'success', App.t('tal.registrado'));
            $('#m-costo, #m-obs').val('');
            buscar(false);
        }).fail(error);
    });

    $('#historial').on('click', '[data-editar]', function () {
        var id = parseInt($(this).attr('data-editar'), 10);
        var m = historial.find(function (x) { return x.id === id; });
        $('#avisos-modal').empty();
        $('#e-id').val(m.id);
        $('#e-costo').val(m.costo);
        $('#e-obs').val(m.observaciones || '');
        modal.show();
    });

    $('#form-edicion').on('submit', function (e) {
        e.preventDefault();
        App.api('PUT', '/api/taller/mantenimientos/' + $('#e-id').val(), {
            costo: parseFloat($('#e-costo').val()),
            observaciones: $('#e-obs').val().trim() || null
        }).done(function () {
            modal.hide();
            App.aviso('#avisos', 'success', App.t('comun.guardado'));
            cargarHistorial();
        }).fail(function (xhr) { App.aviso('#avisos-modal', 'danger', App.mensajeError(xhr)); });
    });
});