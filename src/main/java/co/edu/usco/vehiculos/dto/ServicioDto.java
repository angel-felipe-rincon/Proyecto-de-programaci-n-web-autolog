
package co.edu.usco.vehiculos.dto;

import co.edu.usco.vehiculos.modelo.*;
import jakarta.validation.constraints.*;
/** Datos del catálogo de servicios. */
public class ServicioDto {
    /**
     * Datos para crear o editar un tipo de servicio.
     *
     * @param periodico     indica si el servicio se repite cada cierto kilometraje
     * @param intervaloKm   cada cuántos kilómetros se repite
     * @param margenAvisoKm cuántos kilómetros antes se avisa al cliente
     */
    public record Entrada(@NotBlank @Size(max = 100) String nombre,
                          String descripcion,
                          @Size(max = 60) String categoria,
                          @NotNull AplicaA tipoVehiculo,
                          boolean periodico,
                          @Min(1) Integer intervaloKm,
                          @Min(0) Integer margenAvisoKm) {}
    /** Tipo de servicio tal como lo devuelve la API. */
    public record Respuesta(Long id, String nombre, String descripcion, String categoria,
                            AplicaA tipoVehiculo, boolean periodico, Integer intervaloKm,
                            Integer margenAvisoKm, boolean activo) {
        /**
         * Convierte un tipo de servicio en respuesta.
         *
         * @param t tipo de servicio guardado
         * @return los datos para enviar
         */
        public static Respuesta de(TipoServicio t) {
            return new Respuesta(t.getId(), t.getNombre(), t.getDescripcion(), t.getCategoria(),
                    t.getTipoVehiculo(), t.isPeriodico(), t.getIntervaloKm(),
                    t.getMargenAvisoKm(), t.isActivo());
        }
    }
}