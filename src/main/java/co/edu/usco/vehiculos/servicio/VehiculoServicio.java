
package co.edu.usco.vehiculos.servicio;

import co.edu.usco.vehiculos.dto.VehiculoDto;
import co.edu.usco.vehiculos.modelo.*;
import co.edu.usco.vehiculos.repositorio.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Reglas de los vehículos: lo que el cliente hace con los suyos y lo que puede ver un taller autorizado. */
@Service
@Transactional
public class VehiculoServicio {
    private final VehiculoRepository vehiculos;
    private final UsuarioRepository usuarios;
    private final MantenimientoRepository mantenimientos;
    /** Crea el servicio con sus dependencias. */
    public VehiculoServicio(VehiculoRepository vehiculos, UsuarioRepository usuarios,
                            MantenimientoRepository mantenimientos) {
        this.vehiculos = vehiculos;
        this.usuarios = usuarios;
        this.mantenimientos = mantenimientos;
    }

    
    /**
     * Lista los vehículos de un cliente.
     *
     * @param correo correo del cliente
     * @return sus vehículos
     */
    @Transactional(readOnly = true)
    public List<VehiculoDto.Respuesta> listar(String correo) {
        Usuario c = usuario(correo);
        return vehiculos.findByClienteId(c.getId()).stream()
                .map(v -> VehiculoDto.Respuesta.de(v, true)).toList();
    }
    /**
     * Devuelve un vehículo del cliente.
     *
     * @param id     identificador del vehículo
     * @param correo correo del cliente
     * @return el vehículo con sus talleres autorizados
     */
    @Transactional(readOnly = true)
    public VehiculoDto.Respuesta ver(Long id, String correo) {
        return VehiculoDto.Respuesta.de(propio(id, correo), true);
    }
    /**
     * Registra un vehículo para el cliente. La placa se guarda en mayúsculas y no puede repetirse.
     *
     * @param correo correo del cliente
     * @param d      datos del vehículo
     * @return el vehículo creado
     */
    public VehiculoDto.Respuesta crear(String correo, VehiculoDto.Crear d) {
        Usuario cliente = usuario(correo);
        String placa = d.placa().trim().toUpperCase();
        if (vehiculos.existsByPlaca(placa)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "PLACA_REPETIDA");
        }
        Vehiculo v = new Vehiculo();
        v.setPlaca(placa);
        v.setMarca(d.marca().trim());
        v.setModelo(d.modelo().trim());
        v.setAnio(d.anio());
        v.setTipo(d.tipo());
        v.setKilometraje(d.kilometraje());
        v.setCliente(cliente);
        return VehiculoDto.Respuesta.de(vehiculos.save(v), true);
    }
    /**
     * Elimina un vehículo del cliente. No se permite si ya tiene mantenimientos, para no perder el historial.
     *
     * @param id     identificador del vehículo
     * @param correo correo del cliente
     */
    public void eliminar(Long id, String correo) {
        Vehiculo v = propio(id, correo);
        if (mantenimientos.existsByVehiculoId(id)) {   
            throw new ResponseStatusException(HttpStatus.CONFLICT, "VEHICULO_CON_HISTORIAL");
        }
        vehiculos.delete(v);
    }

    /**
     * Actualiza el kilometraje de un vehículo del cliente. Al guardarlo, el trigger revisa las alertas.
     *
     * @param id     identificador del vehículo
     * @param correo correo del cliente
     * @param km     nuevo kilometraje; no puede ser menor al actual
     * @return el vehículo actualizado
     */
    public VehiculoDto.Respuesta actualizarKm(Long id, String correo, int km) {
        Vehiculo v = propio(id, correo);
        if (km < v.getKilometraje()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "KM_MENOR");
        }
        v.setKilometraje(km);
        return VehiculoDto.Respuesta.de(v, true);
    }

    /**
     * Reemplaza los talleres autorizados de un vehículo: los marcados quedan con acceso y los demás lo pierden.
     *
     * @param id     identificador del vehículo
     * @param correo correo del cliente
     * @param ids    talleres que quedan autorizados
     * @return el vehículo actualizado
     */
    public VehiculoDto.Respuesta fijarTalleres(Long id, String correo, List<Long> ids) {
        Vehiculo v = propio(id, correo);
        Set<Usuario> nuevos = new HashSet<>();
        for (Long tallerId : new HashSet<>(ids)) {
            Usuario t = usuarios.findById(tallerId)
                    .filter(u -> u.getRol() == Rol.TALLER && u.getEstado() == EstadoUsuario.ACTIVO)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "TALLER_NO_VALIDO"));
            nuevos.add(t);
        }
        v.getTalleresAutorizados().clear();
        v.getTalleresAutorizados().addAll(nuevos);
        return VehiculoDto.Respuesta.de(v, true);
    }

    
    /**
     * Busca un vehículo por placa para un taller.
     * Responde 404 si la placa no existe y 403 si el dueño no autorizó a ese taller.
     *
     * @param placa        placa del vehículo
     * @param correoTaller correo del taller
     * @return el vehículo, sin la lista de talleres autorizados
     */
    @Transactional(readOnly = true)
    public VehiculoDto.Respuesta buscarPorPlaca(String placa, String correoTaller) {
        Usuario taller = usuario(correoTaller);
        String p = placa.trim().toUpperCase();
        if (!vehiculos.existsByPlaca(p)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "VEHICULO_NO_ENCONTRADO");
        }
        Vehiculo v = vehiculos.buscarPorPlacaYTaller(p, taller.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "TALLER_NO_AUTORIZADO"));
        return VehiculoDto.Respuesta.de(v, false);
    }

    
    /**
     * Devuelve el vehículo si pertenece al cliente.
     * Responde 404 si no existe y 403 si es de otro cliente.
     *
     * @param id     identificador del vehículo
     * @param correo correo del cliente
     * @return el vehículo
     */
    public Vehiculo propio(Long id, String correo) {
        Vehiculo v = existente(id);
        if (!v.getCliente().getCorreo().equals(correo)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return v;
    }

    /**
     * Devuelve el vehículo si el taller está autorizado. Si no, responde 403.
     *
     * @param id           identificador del vehículo
     * @param correoTaller correo del taller
     * @return el vehículo
     */
    public Vehiculo autorizado(Long id, String correoTaller) {
        Vehiculo v = existente(id);
        boolean permitido = v.getTalleresAutorizados().stream()
                .anyMatch(t -> t.getCorreo().equals(correoTaller));
        if (!permitido) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "TALLER_NO_AUTORIZADO");
        }
        return v;
    }

    private Vehiculo existente(Long id) {
        return vehiculos.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "VEHICULO_NO_ENCONTRADO"));
    }

    private Usuario usuario(String correo) {
        return usuarios.findByCorreo(correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}