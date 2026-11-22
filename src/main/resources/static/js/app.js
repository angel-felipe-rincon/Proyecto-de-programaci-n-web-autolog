var App = (function ($) {
    var textos = {};
    var LOCALES = { es: 'es-CO', en: 'en-US', pt: 'pt-BR' };
    var guardado = localStorage.getItem('idioma');
    var delNavegador = (navigator.language || 'es').slice(0, 2);
    var idioma = LOCALES[guardado] ? guardado : (LOCALES[delNavegador] ? delNavegador : 'es');
    var locale = LOCALES[idioma];

    function t(clave) {
        var s = textos[clave] !== undefined ? textos[clave] : clave;
        for (var i = 1; i < arguments.length; i++) {
            s = s.split('{' + (i - 1) + '}').join(arguments[i]);
        }
        return s;
    }

    function aplicarTextos() {
        $('[data-i18n]').each(function () { $(this).text(t($(this).attr('data-i18n'))); });
        $('[data-i18n-aria]').each(function () { $(this).attr('aria-label', t($(this).attr('data-i18n-aria'))); });
        $('[data-i18n-placeholder]').each(function () { $(this).attr('placeholder', t($(this).attr('data-i18n-placeholder'))); });
        $('[data-i18n-alt]').each(function () { $(this).attr('alt', t($(this).attr('data-i18n-alt'))); });
        $('[data-i18n-title]').each(function () { $(this).attr('title', t($(this).attr('data-i18n-title'))); });
    }

    function iniciar(listo) {
        $.getJSON('/i18n/' + idioma + '.json').done(function (d) {
            textos = d;
            document.documentElement.lang = idioma;
            aplicarTextos();
            $('[data-idioma]').each(function () {
                var activo = $(this).attr('data-idioma') === idioma;
                $(this).toggleClass('active', activo).attr('aria-pressed', activo);
            });
            listo();
        });
    }

    $(document).on('click', '[data-idioma]', function () {
        localStorage.setItem('idioma', $(this).attr('data-idioma'));
        location.reload();
    });

    function esc(v) {
        return String(v == null ? '' : v).replace(/[&<>"']/g, function (c) {
            return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c];
        });
    }
    function num(n) { return n == null ? '?' : Number(n).toLocaleString(locale); }
    function dinero(n) {
        return Number(n).toLocaleString(locale, { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });
    }
    function fecha(iso) { return new Date(iso + 'T00:00:00').toLocaleDateString(locale); }

    function guardarSesion(d) {
        sessionStorage.setItem('token', d.token);
        sessionStorage.setItem('rol', d.rol);
        sessionStorage.setItem('nombre', d.nombre);
    }
    function token() { return sessionStorage.getItem('token'); }
    function nombre() { return sessionStorage.getItem('nombre') || ''; }
    function cerrarSesion() { sessionStorage.clear(); location.href = '/index.html'; }
    function irAlIngreso() { sessionStorage.clear(); location.href = '/ingreso.html'; }
    function irAlPanel(rol) {
        location.href = { CLIENTE: '/cliente.html', TALLER: '/taller.html', ADMIN: '/admin.html' }[rol] || '/index.html';
    }
    function datosDelToken(tk) {
        var p = tk.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
        var texto = atob(p).split('').map(function (c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join('');
        return JSON.parse(decodeURIComponent(texto));
    }
    function sesionActiva() {
        try {
            var d = datosDelToken(token());
            if (d.exp * 1000 > Date.now()) { return d.rol; }
        } catch (e) { }
        return null;
    }
    function exigirRol(rol) {
        if (sesionActiva() === rol) { return true; }
        irAlIngreso();
        return false;
    }

    function api(metodo, url, datos) {
        var o = { method: metodo, url: url, contentType: 'application/json', headers: {} };
        if (token()) { o.headers.Authorization = 'Bearer ' + token(); }
        if (datos !== undefined) { o.data = JSON.stringify(datos); }
        return $.ajax(o);
    }
    $(document).ajaxError(function (e, xhr) { if (xhr.status === 401 && token()) { irAlIngreso(); } });

    function descargar(url, nombreArchivo) {
        return fetch(url, { headers: { Authorization: 'Bearer ' + token() } }).then(function (r) {
            if (!r.ok) { throw new Error(r.status); }
            return r.blob();
        }).then(function (b) {
            var a = document.createElement('a');
            a.href = URL.createObjectURL(b);
            a.download = nombreArchivo;
            document.body.appendChild(a);
            a.click();
            a.remove();
            setTimeout(function () { URL.revokeObjectURL(a.href); }, 1000);
        });
    }

    function mensajeError(xhr) {
        var cod = xhr.responseJSON && xhr.responseJSON.message;
        if (cod && textos['err.' + cod]) { return textos['err.' + cod]; }
        return textos['err.' + xhr.status] || textos['err.generico'];
    }
    function aviso(contenedor, tipo, texto) {
        $(contenedor).html('<div class="alert alert-' + tipo + ' alert-dismissible" role="alert">' + esc(texto) +
            '<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="' + esc(t('comun.cerrar')) + '"></button></div>');
    }

    function filasHistorial(lista, conEditar) {
        if (!lista.length) {
            return '<tr><td colspan="7" class="text-muted py-3">' + esc(t('hist.vacio')) + '</td></tr>';
        }
        return lista.map(function (m) {
            return '<tr><td>' + esc(fecha(m.fecha)) + '</td><td>' + esc(m.servicio) + '</td><td>' + esc(num(m.kilometraje)) +
                '</td><td>' + esc(m.taller) + '</td><td>' + esc(dinero(m.costo)) + '</td><td>' + esc(m.observaciones || '') + '</td>' +
                (conEditar ? '<td>' + (m.editable
                    ? '<button type="button" class="btn btn-outline-primary btn-sm" data-editar="' + m.id + '">' + esc(t('comun.editar')) + '</button>'
                    : '') + '</td>' : '') + '</tr>';
        }).join('');
    }

    return {
        t: t, iniciar: iniciar, esc: esc, num: num, dinero: dinero, fecha: fecha,
        guardarSesion: guardarSesion, token: token, nombre: nombre, cerrarSesion: cerrarSesion,
        irAlIngreso: irAlIngreso, sesionActiva: sesionActiva,
        irAlPanel: irAlPanel, datosDelToken: datosDelToken, exigirRol: exigirRol,
        api: api, descargar: descargar, mensajeError: mensajeError, aviso: aviso,
        filasHistorial: filasHistorial, idioma: function () { return idioma; }
    };
})(jQuery);