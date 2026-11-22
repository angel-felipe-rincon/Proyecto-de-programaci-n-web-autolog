
package co.edu.usco.vehiculos.dto;

import java.time.LocalDateTime;
/** Datos de las alertas que se envían al cliente. */
public class AlertaDto {
    /**
     * Alerta de mantenimiento que ve el cliente.
     *
     * @param proximoKm kilometraje en el que toca hacer el servicio
     */
    public record Respuesta(Long id, Long vehiculoId, String placa, String servicio,
                            Integer proximoKm, LocalDateTime fechaCreacion) {}
}
