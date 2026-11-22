
package co.edu.usco.vehiculos.repositorio;

import co.edu.usco.vehiculos.modelo.Alerta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
/** Acceso a las alertas guardadas. */
public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    /**
     * Busca las alertas activas de todos los vehículos de un cliente.
     *
     * @param clienteId identificador del cliente
     * @return sus alertas activas
     */
	List<Alerta> findByVehiculoClienteIdAndActivaTrue(Long clienteId);
    /**
     * Busca las alertas activas de un vehículo.
     *
     * @param vehiculoId identificador del vehículo
     * @return sus alertas activas
     */
    List<Alerta> findByVehiculoIdAndActivaTrue(Long vehiculoId);

    /**
     * Desactiva la alerta de un servicio cuando el taller lo registra.
     *
     * @param vehiculoId identificador del vehículo
     * @param tipoId     identificador del tipo de servicio
     * @return cuántas alertas se desactivaron
     */
    @Modifying
    @Query("update Alerta a set a.activa = false where a.vehiculo.id = :vehiculoId "
         + "and a.tipoServicio.id = :tipoId and a.activa = true")
    int desactivar(@Param("vehiculoId") Long vehiculoId, @Param("tipoId") Long tipoId);
}
