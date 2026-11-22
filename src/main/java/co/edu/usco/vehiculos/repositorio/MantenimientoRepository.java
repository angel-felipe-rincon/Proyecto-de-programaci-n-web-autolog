package co.edu.usco.vehiculos.repositorio;

import co.edu.usco.vehiculos.modelo.Mantenimiento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
/** Acceso a los mantenimientos guardados. */
public interface MantenimientoRepository extends JpaRepository<Mantenimiento, Long> {
    /**
     * Busca el historial de un vehículo, del más reciente al más antiguo.
     *
     * @param vehiculoId identificador del vehículo
     * @return sus mantenimientos
     */
	List<Mantenimiento> findByVehiculoIdOrderByFechaDescIdDesc(Long vehiculoId);
    /**
     * Indica si un vehículo ya tiene mantenimientos.
     *
     * @param vehiculoId identificador del vehículo
     * @return true si tiene al menos uno
     */
    boolean existsByVehiculoId(Long vehiculoId);

    /**
     * Devuelve el último kilometraje en que se le hizo un servicio a un vehículo.
     * Con él se calcula el próximo aviso.
     *
     * @param vehiculoId identificador del vehículo
     * @param tipoId     identificador del tipo de servicio
     * @return el kilometraje más alto registrado, o null si nunca se ha hecho
     */
    @Query("select max(m.kilometraje) from Mantenimiento m "
         + "where m.vehiculo.id = :vehiculoId and m.tipoServicio.id = :tipoId")
    Integer ultimoKm(@Param("vehiculoId") Long vehiculoId, @Param("tipoId") Long tipoId);
}
