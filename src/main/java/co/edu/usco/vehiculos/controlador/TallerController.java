
package co.edu.usco.vehiculos.controlador;

import co.edu.usco.vehiculos.dto.*;
import co.edu.usco.vehiculos.servicio.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** Servicios del taller sobre los vehículos cuyos dueños lo autorizaron. */
@RestController
@RequestMapping("/api/taller")
@Tag(name = "Taller")
public class TallerController {
    private final VehiculoServicio vehiculos;
    private final MantenimientoServicio mantenimientos;
    /** Crea el controlador con sus dependencias. */
    public TallerController(VehiculoServicio vehiculos, MantenimientoServicio mantenimientos) {
        this.vehiculos = vehiculos;
        this.mantenimientos = mantenimientos;
    }
    /**
     * Busca un vehículo por su placa.
     * Responde 404 si no existe y 403 si el dueño no ha autorizado a este taller.
     *
     * @param placa placa del vehículo
     * @param auth  usuario autenticado
     * @return el vehículo encontrado
     */
    @GetMapping("/vehiculos/buscar")
    @Operation(summary = "Busca por placa (404 si no existe, 403 si no autorizo a este taller)")
    public VehiculoDto.Respuesta buscar(@RequestParam String placa, Authentication auth) {
        return vehiculos.buscarPorPlaca(placa, auth.getName());
    }
    /**
     * Lista los servicios activos que se pueden aplicar a este vehículo.
     *
     * @param id   identificador del vehículo
     * @param auth usuario autenticado
     * @return los servicios disponibles
     */
    @GetMapping("/vehiculos/{id}/servicios")
    @Operation(summary = "Servicios activos que aplican a este vehiculo")
    public List<ServicioDto.Respuesta> servicios(@PathVariable Long id, Authentication auth) {
        return mantenimientos.serviciosAplicables(id, auth.getName());
    }
    /**
     * Devuelve el historial de un vehículo que autorizó a este taller.
     *
     * @param id   identificador del vehículo
     * @param auth usuario autenticado
     * @return los mantenimientos, del más reciente al más antiguo
     */
    @GetMapping("/vehiculos/{id}/mantenimientos")
    @Operation(summary = "Historial de un vehiculo autorizado")
    public List<MantenimientoDto.Respuesta> historial(@PathVariable Long id, Authentication auth) {
        return mantenimientos.historialTaller(id, auth.getName());
    }
    /**
     * Registra un mantenimiento y actualiza el kilometraje del vehículo.
     *
     * @param id   identificador del vehículo
     * @param d    datos del mantenimiento
     * @param auth usuario autenticado
     * @return el mantenimiento creado, con estado 201
     */
    @PostMapping("/vehiculos/{id}/mantenimientos")
    @Operation(summary = "Registra un mantenimiento y actualiza el kilometraje del vehiculo")
    public ResponseEntity<MantenimientoDto.Respuesta> registrar(@PathVariable Long id,
            @Valid @RequestBody MantenimientoDto.Crear d, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mantenimientos.registrar(id, auth.getName(), d));
    }
    /**
     * Corrige el costo y las observaciones de un mantenimiento registrado por este taller.
     *
     * @param id   identificador del mantenimiento
     * @param d    nuevo costo y observaciones
     * @param auth usuario autenticado
     * @return el mantenimiento actualizado
     */
    @PutMapping("/mantenimientos/{id}")
    @Operation(summary = "Corrige costo y observaciones de un mantenimiento propio")
    public MantenimientoDto.Respuesta editar(@PathVariable Long id,
            @Valid @RequestBody MantenimientoDto.Editar d, Authentication auth) {
        return mantenimientos.editar(id, auth.getName(), d);
    }
}