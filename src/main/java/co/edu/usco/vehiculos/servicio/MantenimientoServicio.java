
package co.edu.usco.vehiculos.servicio;

import co.edu.usco.vehiculos.dto.MantenimientoDto;
import co.edu.usco.vehiculos.dto.ServicioDto;
import co.edu.usco.vehiculos.modelo.*;
import co.edu.usco.vehiculos.repositorio.*;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Registro y consulta de mantenimientos. El kilometraje de un vehículo nunca puede disminuir. */
@Service
@Transactional
public class MantenimientoServicio {
    private final MantenimientoRepository mantenimientos;
    private final TipoServicioRepository tipos;
    private final AlertaRepository alertas;
    private final UsuarioRepository usuarios;
    private final VehiculoServicio vehiculoServicio;
    /** Crea el servicio con sus dependencias. */
    public MantenimientoServicio(MantenimientoRepository mantenimientos, TipoServicioRepository tipos,
                                 AlertaRepository alertas, UsuarioRepository usuarios,
                                 VehiculoServicio vehiculoServicio) {
        this.mantenimientos = mantenimientos;
        this.tipos = tipos;
        this.alertas = alertas;
        this.usuarios = usuarios;
        this.vehiculoServicio = vehiculoServicio;
    }
    /**
     * Devuelve el historial de un vehículo a su dueño.
     *
     * @param vehiculoId identificador del vehículo
     * @param correo     correo del cliente
     * @return los mantenimientos, del más reciente al más antiguo
     */
    @Transactional(readOnly = true)
    public List<MantenimientoDto.Respuesta> historialCliente(Long vehiculoId, String correo) {
        vehiculoServicio.propio(vehiculoId, correo);
        return listar(vehiculoId, null);
    }
    /**
     * Devuelve el historial de un vehículo a un taller autorizado.
     *
     * @param vehiculoId   identificador del vehículo
     * @param correoTaller correo del taller
     * @return los mantenimientos; los que registró este taller se pueden corregir
     */
    @Transactional(readOnly = true)
    public List<MantenimientoDto.Respuesta> historialTaller(Long vehiculoId, String correoTaller) {
        vehiculoServicio.autorizado(vehiculoId, correoTaller);
        return listar(vehiculoId, correoTaller);
    }

    /**
     * Lista los servicios activos que aplican al tipo de vehículo, para que el taller elija uno.
     *
     * @param vehiculoId   identificador del vehículo
     * @param correoTaller correo del taller
     * @return los servicios disponibles
     */
    @Transactional(readOnly = true)
    public List<ServicioDto.Respuesta> serviciosAplicables(Long vehiculoId, String correoTaller) {
        Vehiculo v = vehiculoServicio.autorizado(vehiculoId, correoTaller);
        List<AplicaA> aplica = List.of(AplicaA.valueOf(v.getTipo().name()), AplicaA.AMBOS);
        return tipos.findByActivoTrueAndTipoVehiculoIn(aplica).stream()
                .map(ServicioDto.Respuesta::de).toList();
    }

    /**
     * Registra un mantenimiento y actualiza el kilometraje del vehículo.
     * Primero guarda el mantenimiento, luego desactiva la alerta de ese servicio y al final
     * cambia el kilometraje, que es lo que hace que el trigger revise las alertas.
     *
     * @param vehiculoId   identificador del vehículo
     * @param correoTaller correo del taller
     * @param d            datos del mantenimiento
     * @return el mantenimiento creado
     */
    public MantenimientoDto.Respuesta registrar(Long vehiculoId, String correoTaller, MantenimientoDto.Crear d) {
        Vehiculo v = vehiculoServicio.autorizado(vehiculoId, correoTaller);
        Usuario taller = usuarios.findByCorreo(correoTaller)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        TipoServicio ts = tipos.findById(d.tipoServicioId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SERVICIO_NO_ENCONTRADO"));
        if (!ts.isActivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "SERVICIO_INACTIVO");
        }
        if (ts.getTipoVehiculo() != AplicaA.AMBOS && !ts.getTipoVehiculo().name().equals(v.getTipo().name())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "SERVICIO_NO_APLICA");
        }
        if (d.kilometraje() < v.getKilometraje()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "KM_MENOR");
        }

        Mantenimiento m = new Mantenimiento();
        m.setVehiculo(v);
        m.setTaller(taller);
        m.setTipoServicio(ts);
        m.setFecha(d.fecha() != null ? d.fecha() : LocalDate.now());
        m.setKilometraje(d.kilometraje());
        m.setCosto(d.costo());
        m.setObservaciones(d.observaciones());
        mantenimientos.saveAndFlush(m);              

        alertas.desactivar(v.getId(), ts.getId());   
        v.setKilometraje(d.kilometraje());           
        return MantenimientoDto.Respuesta.de(m, correoTaller);
    }

    /**
     * Corrige el costo y las observaciones de un mantenimiento.
     * Solo puede hacerlo el taller que lo registró y mientras siga autorizado.
     *
     * @param id           identificador del mantenimiento
     * @param correoTaller correo del taller
     * @param d            nuevo costo y observaciones
     * @return el mantenimiento actualizado
     */
    public MantenimientoDto.Respuesta editar(Long id, String correoTaller, MantenimientoDto.Editar d) {
        Mantenimiento m = mantenimientos.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "MANTENIMIENTO_NO_ENCONTRADO"));
        if (!m.getTaller().getCorreo().equals(correoTaller)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        vehiculoServicio.autorizado(m.getVehiculo().getId(), correoTaller);
        m.setCosto(d.costo());
        m.setObservaciones(d.observaciones());
        return MantenimientoDto.Respuesta.de(m, correoTaller);
    }

    private List<MantenimientoDto.Respuesta> listar(Long vehiculoId, String correoTaller) {
        return mantenimientos.findByVehiculoIdOrderByFechaDescIdDesc(vehiculoId).stream()
                .map(m -> MantenimientoDto.Respuesta.de(m, correoTaller)).toList();
    }
}