
package co.edu.usco.vehiculos.dto;

import co.edu.usco.vehiculos.modelo.Mantenimiento;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
/** Datos de mantenimientos que entran y salen de la API. */
public class MantenimientoDto {
    /**
     * Datos para registrar un mantenimiento.
     *
     * @param fecha fecha del servicio; si no se envía, se usa la de hoy
     */
    public record Crear(@NotNull Long tipoServicioId,
                        @NotNull @Min(0) Integer kilometraje,
                        @NotNull @DecimalMin("0.0") @Digits(integer = 10, fraction = 2) BigDecimal costo,
                        @Size(max = 2000) String observaciones,
                        @PastOrPresent LocalDate fecha) {}
    /** Corrección que puede hacer el taller: solo costo y observaciones. */
    public record Editar(@NotNull @DecimalMin("0.0") @Digits(integer = 10, fraction = 2) BigDecimal costo,
                         @Size(max = 2000) String observaciones) {}

    /**
     * Mantenimiento tal como se muestra en el historial.
     *
     * @param editable indica si el taller que consulta fue quien lo registró y por eso puede corregirlo
     */
    public record Respuesta(Long id, LocalDate fecha, Integer kilometraje, BigDecimal costo,
                            String observaciones, String servicio, String taller, boolean editable) {
        /**
         * Convierte un mantenimiento en respuesta.
         *
         * @param m            mantenimiento guardado
         * @param correoTaller correo del taller que consulta, o null si consulta el cliente
         * @return los datos para enviar
         */
        public static Respuesta de(Mantenimiento m, String correoTaller) {
            boolean propio = correoTaller != null && m.getTaller().getCorreo().equals(correoTaller);
            return new Respuesta(m.getId(), m.getFecha(), m.getKilometraje(), m.getCosto(),
                    m.getObservaciones(), m.getTipoServicio().getNombre(), m.getTaller().getNombre(), propio);
        }
    }
}