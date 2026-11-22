
package co.edu.usco.vehiculos.servicio;

import co.edu.usco.vehiculos.dto.AlertaDto;
import co.edu.usco.vehiculos.modelo.Usuario;
import co.edu.usco.vehiculos.repositorio.AlertaRepository;
import co.edu.usco.vehiculos.repositorio.MantenimientoRepository;
import co.edu.usco.vehiculos.repositorio.UsuarioRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Consulta las alertas de los clientes. Las alertas las crea un trigger de la base de datos. */
@Service
public class AlertaServicio {
    private final AlertaRepository alertas;
    private final UsuarioRepository usuarios;
    private final MantenimientoRepository mantenimientos;
    /** Crea el servicio con sus dependencias. */
    public AlertaServicio(AlertaRepository alertas, UsuarioRepository usuarios,
                          MantenimientoRepository mantenimientos) {
        this.alertas = alertas;
        this.usuarios = usuarios;
        this.mantenimientos = mantenimientos;
    }
    /**
     * Lista las alertas activas de un cliente y calcula el kilometraje del próximo servicio.
     *
     * @param correo correo del cliente
     * @return sus alertas
     */
    @Transactional(readOnly = true)
    public List<AlertaDto.Respuesta> delCliente(String correo) {
        Usuario c = usuarios.findByCorreo(correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return alertas.findByVehiculoClienteIdAndActivaTrue(c.getId()).stream().map(a -> {
            Integer ultimo = mantenimientos.ultimoKm(a.getVehiculo().getId(), a.getTipoServicio().getId());
            Integer intervalo = a.getTipoServicio().getIntervaloKm();
            Integer proximo = (ultimo != null && intervalo != null) ? ultimo + intervalo : null;
            return new AlertaDto.Respuesta(a.getId(), a.getVehiculo().getId(), a.getVehiculo().getPlaca(),
                    a.getTipoServicio().getNombre(), proximo, a.getFechaCreacion());
        }).toList();
    }
}
