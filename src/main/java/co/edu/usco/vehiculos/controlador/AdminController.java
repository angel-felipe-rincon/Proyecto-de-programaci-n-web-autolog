
package co.edu.usco.vehiculos.controlador;

import co.edu.usco.vehiculos.dto.*;
import co.edu.usco.vehiculos.modelo.*;
import co.edu.usco.vehiculos.repositorio.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/** Servicios del administrador: aprobación de talleres y catálogo de servicios. */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Administrador")
public class AdminController {
    private final UsuarioRepository usuarios;
    private final TipoServicioRepository tipos;
    /** Crea el controlador con sus dependencias. */
    public AdminController(UsuarioRepository usuarios, TipoServicioRepository tipos) {
        this.usuarios = usuarios;
        this.tipos = tipos;
    }
    /**
     * Lista todos los usuarios del sistema, sin sus contraseñas.
     *
     * @return los usuarios registrados
     */
    @GetMapping("/usuarios")
    @Operation(summary = "Lista todos los usuarios (sin claves)")
    public List<UsuarioDto.Resumen> listarUsuarios() {
        return usuarios.findAll().stream().map(UsuarioDto.Resumen::de).toList();
    }
    /**
     * Lista los talleres registrados.
     *
     * @param estado si se envía, solo devuelve los talleres en ese estado
     * @return los talleres encontrados
     */
    @GetMapping("/talleres")
    @Operation(summary = "Lista los talleres; filtro opcional por estado (PENDIENTE, ACTIVO, INACTIVO)")
    public List<UsuarioDto.Resumen> listarTalleres(@RequestParam(required = false) EstadoUsuario estado) {
        List<Usuario> lista = estado == null
                ? usuarios.findByRol(Rol.TALLER)
                : usuarios.findByRolAndEstado(Rol.TALLER, estado);
        return lista.stream().map(UsuarioDto.Resumen::de).toList();
    }
    /**
     * Aprueba un taller pendiente o reactiva uno desactivado.
     *
     * @param id identificador del taller
     * @return el taller con su nuevo estado
     */
    @PutMapping("/talleres/{id}/aprobar")
    @Operation(summary = "Aprueba (o reactiva) un taller")
    public UsuarioDto.Resumen aprobar(@PathVariable Long id) {
        return cambiarEstado(id, EstadoUsuario.ACTIVO);
    }
    /**
     * Desactiva un taller. Desde ese momento ya no puede ingresar.
     *
     * @param id identificador del taller
     * @return el taller con su nuevo estado
     */
    @PutMapping("/talleres/{id}/desactivar")
    @Operation(summary = "Desactiva un taller: pierde el acceso de inmediato")
    public UsuarioDto.Resumen desactivar(@PathVariable Long id) {
        return cambiarEstado(id, EstadoUsuario.INACTIVO);
    }
    /**
     * Lista todos los tipos de servicio, activos e inactivos.
     *
     * @return el catálogo completo
     */
    @GetMapping("/servicios")
    @Operation(summary = "Catalogo completo de tipos de servicio")
    public List<ServicioDto.Respuesta> listarServicios() {
        return tipos.findAll().stream().map(ServicioDto.Respuesta::de).toList();
    }
    /**
     * Agrega un tipo de servicio al catálogo.
     *
     * @param d datos del servicio
     * @return el servicio creado, con estado 201
     */
    @PostMapping("/servicios")
    @Operation(summary = "Crea un tipo de servicio")
    public ResponseEntity<ServicioDto.Respuesta> crearServicio(@Valid @RequestBody ServicioDto.Entrada d) {
        TipoServicio t = new TipoServicio();
        aplicar(t, d);
        return ResponseEntity.status(HttpStatus.CREATED).body(ServicioDto.Respuesta.de(tipos.save(t)));
    }
    /**
     * Modifica un tipo de servicio del catálogo.
     *
     * @param id identificador del servicio
     * @param d  nuevos datos del servicio
     * @return el servicio actualizado
     */
    @PutMapping("/servicios/{id}")
    @Operation(summary = "Edita un tipo de servicio")
    public ServicioDto.Respuesta editarServicio(@PathVariable Long id, @Valid @RequestBody ServicioDto.Entrada d) {
        TipoServicio t = tipos.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SERVICIO_NO_ENCONTRADO"));
        aplicar(t, d);
        return ServicioDto.Respuesta.de(tipos.save(t));
    }
    /**
     * Activa o desactiva un tipo de servicio.
     *
     * @param id    identificador del servicio
     * @param valor true para activarlo, false para desactivarlo
     * @return el servicio actualizado
     */
    @PutMapping("/servicios/{id}/activo")
    @Operation(summary = "Activa o desactiva un servicio (valor=true o false)")
    public ServicioDto.Respuesta activo(@PathVariable Long id, @RequestParam boolean valor) {
        TipoServicio t = tipos.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SERVICIO_NO_ENCONTRADO"));
        t.setActivo(valor);
        return ServicioDto.Respuesta.de(tipos.save(t));
    }

    private UsuarioDto.Resumen cambiarEstado(Long id, EstadoUsuario estado) {
        Usuario t = usuarios.findById(id).filter(u -> u.getRol() == Rol.TALLER).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "TALLER_NO_ENCONTRADO"));
        t.setEstado(estado);
        return UsuarioDto.Resumen.de(usuarios.save(t));
    }

    /**
     * Copia los datos recibidos al tipo de servicio.
     * Si es periódico, exige un intervalo mayor que cero y un margen de aviso menor que ese intervalo.
     */
    private void aplicar(TipoServicio t, ServicioDto.Entrada d) {
        if (d.periodico() && (d.intervaloKm() == null || d.margenAvisoKm() == null
                || d.intervaloKm() <= 0 || d.margenAvisoKm() < 0 || d.margenAvisoKm() >= d.intervaloKm())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INTERVALO_INVALIDO");
        }
        t.setNombre(d.nombre().trim());
        t.setDescripcion(d.descripcion());
        t.setCategoria(d.categoria());
        t.setTipoVehiculo(d.tipoVehiculo());
        t.setPeriodico(d.periodico());
        t.setIntervaloKm(d.periodico() ? d.intervaloKm() : null);
        t.setMargenAvisoKm(d.periodico() ? d.margenAvisoKm() : null);
    }
}
