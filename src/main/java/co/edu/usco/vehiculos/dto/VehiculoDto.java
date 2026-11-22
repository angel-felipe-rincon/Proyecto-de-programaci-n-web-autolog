
package co.edu.usco.vehiculos.dto;

import co.edu.usco.vehiculos.modelo.*;
import jakarta.validation.constraints.*;
import java.util.List;
/** Datos de vehículos que entran y salen de la API. */
public class VehiculoDto {
    /** Datos para registrar un vehículo. Después de creado solo cambia su kilometraje. */
    public record Crear(@NotBlank @Size(max = 10) String placa,
                        @NotBlank @Size(max = 60) String marca,
                        @NotBlank @Size(max = 60) String modelo,
                        @NotNull @Min(1950) @Max(2100) Integer anio,
                        @NotNull TipoVehiculo tipo,
                        @NotNull @Min(0) Integer kilometraje) {}

    /** Nuevo kilometraje de un vehículo. */
    public record Km(@NotNull @Min(0) Integer kilometraje) {}
    /** Talleres que el cliente autoriza para un vehículo. */
    public record Talleres(@NotNull List<@NotNull Long> ids) {}
    /**
     * Vehículo tal como lo devuelve la API.
     *
     * @param talleresAutorizados talleres que pueden atenderlo; no se envía cuando consulta un taller
     */
    public record Respuesta(Long id, String placa, String marca, String modelo, Integer anio,
                            TipoVehiculo tipo, Integer kilometraje, List<Long> talleresAutorizados) {
        /**
         * Convierte un vehículo en respuesta.
         * La lista de talleres solo se incluye para el dueño: un taller no debe saber qué otros talleres tienen acceso.
         *
         * @param v               vehículo guardado
         * @param incluirTalleres true si quien consulta es el dueño
         * @return los datos para enviar
         */
        public static Respuesta de(Vehiculo v, boolean incluirTalleres) {
            List<Long> ids = incluirTalleres
                    ? v.getTalleresAutorizados().stream().map(Usuario::getId).toList() : null;
            return new Respuesta(v.getId(), v.getPlaca(), v.getMarca(), v.getModelo(),
                    v.getAnio(), v.getTipo(), v.getKilometraje(), ids);
        }
    }
}