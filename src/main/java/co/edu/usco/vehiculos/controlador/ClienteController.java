
package co.edu.usco.vehiculos.controlador;

import co.edu.usco.vehiculos.dto.*;
import co.edu.usco.vehiculos.modelo.*;
import co.edu.usco.vehiculos.repositorio.UsuarioRepository;
import co.edu.usco.vehiculos.servicio.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Servicios del cliente para manejar sus propios vehículos.
 * Placa, marca, modelo, año y tipo no se modifican después de registrar el vehículo.
 */
@RestController
@RequestMapping("/api/cliente")
@Tag(name = "Cliente")
public class ClienteController {
    private final VehiculoServicio vehiculos;
    private final MantenimientoServicio mantenimientos;
    private final AlertaServicio alertas;
    private final UsuarioRepository usuarios;
    /** Crea el controlador con sus dependencias. */
    public ClienteController(VehiculoServicio vehiculos, MantenimientoServicio mantenimientos,
                             AlertaServicio alertas, UsuarioRepository usuarios) {
        this.vehiculos = vehiculos;
        this.mantenimientos = mantenimientos;
        this.alertas = alertas;
        this.usuarios = usuarios;
    }
    /**
     * Lista los vehículos del cliente.
     *
     * @param auth usuario autenticado
     * @return sus vehículos
     */
    @GetMapping("/vehiculos")
    @Operation(summary = "Lista los vehiculos del cliente")
    public List<VehiculoDto.Respuesta> misVehiculos(Authentication auth) {
        return vehiculos.listar(auth.getName());
    }
    /**
     * Registra un vehículo nuevo. La placa no puede estar repetida.
     *
     * @param d    datos del vehículo
     * @param auth usuario autenticado
     * @return el vehículo creado, con estado 201
     */
    @PostMapping("/vehiculos")
    @Operation(summary = "Registra un vehiculo (la placa es unica)")
    public ResponseEntity<VehiculoDto.Respuesta> crear(@Valid @RequestBody VehiculoDto.Crear d, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehiculos.crear(auth.getName(), d));
    }
    /**
     * Devuelve los datos de un vehículo del cliente.
     *
     * @param id   identificador del vehículo
     * @param auth usuario autenticado
     * @return el vehículo con sus talleres autorizados
     */
    @GetMapping("/vehiculos/{id}")
    @Operation(summary = "Ficha de un vehiculo propio (403 si es de otro cliente)")
    public VehiculoDto.Respuesta ver(@PathVariable Long id, Authentication auth) {
        return vehiculos.ver(id, auth.getName());
    }
    /**
     * Elimina un vehículo que aún no tiene mantenimientos.
     *
     * @param id   identificador del vehículo
     * @param auth usuario autenticado
     * @return respuesta vacía con estado 204
     */
    @DeleteMapping("/vehiculos/{id}")
    @Operation(summary = "Elimina un vehiculo sin historial")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication auth) {
        vehiculos.eliminar(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
    /**
     * Actualiza el kilometraje de un vehículo. No acepta un valor menor al actual.
     *
     * @param id   identificador del vehículo
     * @param d    nuevo kilometraje
     * @param auth usuario autenticado
     * @return el vehículo actualizado
     */
    @PatchMapping("/vehiculos/{id}/kilometraje")
    @Operation(summary = "Actualiza el kilometraje (rechaza un valor menor al actual)")
    public VehiculoDto.Respuesta kilometraje(@PathVariable Long id, @Valid @RequestBody VehiculoDto.Km d,
                                             Authentication auth) {
        return vehiculos.actualizarKm(id, auth.getName(), d.kilometraje());
    }
    /**
     * Lista los talleres aprobados que el cliente puede autorizar.
     *
     * @return identificador y nombre de cada taller
     */
    @GetMapping("/talleres")
    @Operation(summary = "Talleres aprobados que se pueden autorizar")
    public List<UsuarioDto.Publico> talleres() {
        return usuarios.findByRolAndEstado(Rol.TALLER, EstadoUsuario.ACTIVO).stream()
                .map(UsuarioDto.Publico::de).toList();
    }
    /**
     * Define qué talleres pueden atender el vehículo. Reemplaza la lista anterior.
     *
     * @param id   identificador del vehículo
     * @param d    talleres autorizados
     * @param auth usuario autenticado
     * @return el vehículo actualizado
     */
    @PutMapping("/vehiculos/{id}/talleres")
    @Operation(summary = "Fija los talleres autorizados para este vehiculo")
    public VehiculoDto.Respuesta autorizar(@PathVariable Long id, @Valid @RequestBody VehiculoDto.Talleres d,
                                           Authentication auth) {
        return vehiculos.fijarTalleres(id, auth.getName(), d.ids());
    }
    /**
     * Devuelve el historial de mantenimientos de un vehículo propio.
     *
     * @param id   identificador del vehículo
     * @param auth usuario autenticado
     * @return los mantenimientos, del más reciente al más antiguo
     */
    @GetMapping("/vehiculos/{id}/mantenimientos")
    @Operation(summary = "Historial de mantenimiento del vehiculo")
    public List<MantenimientoDto.Respuesta> historial(@PathVariable Long id, Authentication auth) {
        return mantenimientos.historialCliente(id, auth.getName());
    }
    /**
     * Lista las alertas activas de todos los vehículos del cliente.
     *
     * @param auth usuario autenticado
     * @return las alertas pendientes
     */
    @GetMapping("/alertas")
    @Operation(summary = "Alertas activas de todos los vehiculos del cliente")
    public List<AlertaDto.Respuesta> misAlertas(Authentication auth) {
        return alertas.delCliente(auth.getName());
    }
}