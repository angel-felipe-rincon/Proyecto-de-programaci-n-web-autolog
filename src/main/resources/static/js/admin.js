$(function () {
    if (!App.exigirRol('ADMIN')) { return; }
    var modal = new bootstrap.Modal(document.getElementById('modal-servicio'));
    var servicios = [];

    App.iniciar(function () {
        $('#saludo').text(App.t('comun.hola', App.nombre()));
        $('#salir').on('click', App.cerrarSesion);
        cargarTalleres();
        cargarServicios();
    });

    function error(xhr) { App.aviso('#avisos', 'danger', App.mensajeError(xhr)); }

    function insignia(estado) {
        var color = { PENDIENTE: 'text-bg-warning', ACTIVO: 'text-bg-success', INACTIVO: 'text-bg-secondary' }[estado];
        return '<span class="badge ' + color + '">' + App.esc(App.t('estado.' + estado)) + '</span>';
    }

    function cargarTalleres() {
        App.api('GET', '/api/admin/talleres').done(function (lista) {
            var orden = { PENDIENTE: 0, ACTIVO: 1, INACTIVO: 2 };
            lista.sort(function (a, b) { return orden[a.estado] - orden[b.estado]; });
            var pendientes = lista.filter(function (u) { return u.estado === 'PENDIENTE'; }).length;
            $('#resumen-talleres').html(pendientes
                ? '<span class="badge text-bg-warning">' + App.esc(App.t('adm.hayPendientes', pendientes)) + '</span>'
                : '<span class="text-success"><i class="bi bi-check-circle" aria-hidden="true"></i> ' +
                  App.esc(App.t('adm.sinPendientes')) + '</span>');

            $('#tabla-talleres').html(lista.length ? lista.map(function (u) {
                var accion = u.estado === 'ACTIVO'
                    ? '<button type="button" class="btn btn-outline-danger btn-sm" data-taller="' + u.id +
                      '" data-accion="desactivar">' + App.esc(App.t('adm.desactivar')) + '</button>'
                    : '<button type="button" class="btn btn-outline-success btn-sm" data-taller="' + u.id +
                      '" data-accion="aprobar">' + App.esc(App.t(u.estado === 'PENDIENTE' ? 'adm.aprobar' : 'adm.reactivar')) + '</button>';
                return '<tr><td>' + App.esc(u.nombre) + '</td><td>' + App.esc(u.correo) + '</td><td>' +
                    insignia(u.estado) + '</td><td>' + accion + '</td></tr>';
            }).join('') : '<tr><td colspan="4" class="text-muted py-3">' + App.esc(App.t('adm.sinTalleres')) + '</td></tr>');
        });
    }
    $('#tabla-talleres').on('click', '[data-taller]', function () {
        App.api('PUT', '/api/admin/talleres/' + $(this).attr('data-taller') + '/' + $(this).attr('data-accion'))
            .done(function () { App.aviso('#avisos', 'success', App.t('comun.guardado')); cargarTalleres(); }).fail(error);
    });

    function cargarServicios() {
        App.api('GET', '/api/admin/servicios').done(function (lista) {
            servicios = lista.sort(function (a, b) { return a.nombre.localeCompare(b.nombre); });
            $('#tabla-servicios').html(lista.length ? lista.map(function (s) {
                return '<tr><td>' + App.esc(s.nombre) + '</td><td>' + App.esc(s.categoria || '') + '</td><td>' +
                    App.esc(App.t('tipo.' + s.tipoVehiculo)) + '</td><td>' +
                    (s.periodico ? App.esc(App.t('adm.cadaKm', App.num(s.intervaloKm), App.num(s.margenAvisoKm))) : '—') +
                    '</td><td>' + insignia(s.activo ? 'ACTIVO' : 'INACTIVO') + '</td><td>' +
                    '<button type="button" class="btn btn-outline-primary btn-sm me-1" data-editar="' + s.id + '">' +
                    App.esc(App.t('comun.editar')) + '</button>' +
                    '<button type="button" class="btn btn-outline-secondary btn-sm" data-activo="' + s.id +
                    '" data-valor="' + (!s.activo) + '">' + App.esc(App.t(s.activo ? 'adm.desactivar' : 'adm.activar')) +
                    '</button></td></tr>';
            }).join('') : '<tr><td colspan="6" class="text-muted py-3">' + App.esc(App.t('adm.sinServicios')) + '</td></tr>');
        });
    }

    $('#tabla-servicios').on('click', '[data-activo]', function () {
        App.api('PUT', '/api/admin/servicios/' + $(this).attr('data-activo') + '/activo?valor=' + $(this).attr('data-valor'))
            .done(function () { cargarServicios(); }).fail(error);
    });
    $('#tabla-servicios').on('click', '[data-editar]', function () {
        var id = parseInt($(this).attr('data-editar'), 10);
        abrirModal(servicios.find(function (s) { return s.id === id; }));
    });
    $('#btn-nuevo').on('click', function () { abrirModal(null); });
    $('#s-periodico').on('change', alternarPeriodico);

    function alternarPeriodico() {
        var p = $('#s-periodico').is(':checked');
        $('#grupo-periodico').toggleClass('d-none', !p);
        $('#s-intervalo, #s-margen').prop('disabled', !p).prop('required', p);
    }

    function abrirModal(s) {
        $('#avisos-modal').empty();
        $('#form-servicio')[0].reset();
        $('#s-id').val(s ? s.id : '');
        $('#t-modal').text(App.t(s ? 'adm.editarServicio' : 'adm.nuevoServicio'));
        if (s) {
            $('#s-nombre').val(s.nombre); $('#s-desc').val(s.descripcion); $('#s-categoria').val(s.categoria);
            $('#s-tipo').val(s.tipoVehiculo); $('#s-periodico').prop('checked', s.periodico);
            $('#s-intervalo').val(s.intervaloKm); $('#s-margen').val(s.margenAvisoKm);
        }
        alternarPeriodico();
        modal.show();
    }

    $('#form-servicio').on('submit', function (e) {
        e.preventDefault();
        var id = $('#s-id').val(), p = $('#s-periodico').is(':checked');
        var d = {
            nombre: $('#s-nombre').val(), descripcion: $('#s-desc').val() || null,
            categoria: $('#s-categoria').val() || null, tipoVehiculo: $('#s-tipo').val(), periodico: p,
            intervaloKm: p ? parseInt($('#s-intervalo').val(), 10) : null,
            margenAvisoKm: p ? parseInt($('#s-margen').val(), 10) : null
        };
        App.api(id ? 'PUT' : 'POST', id ? '/api/admin/servicios/' + id : '/api/admin/servicios', d)
            .done(function () {
                modal.hide();
                App.aviso('#avisos', 'success', App.t('comun.guardado'));
                cargarServicios();
            }).fail(function (xhr) { App.aviso('#avisos-modal', 'danger', App.mensajeError(xhr)); });
    });
});