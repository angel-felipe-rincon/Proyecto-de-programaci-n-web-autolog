
package co.edu.usco.vehiculos.repositorio;

import co.edu.usco.vehiculos.modelo.Vehiculo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
/** Acceso a los vehículos guardados. */
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {
    /**
     * Busca los vehículos de un cliente.
     *
     * @param clienteId identificador del cliente
     * @return sus vehículos
     */
	List<Vehiculo> findByClienteId(Long clienteId);
    /**
     * Indica si ya hay un vehículo con esa placa.
     *
     * @param placa placa a revisar
     * @return true si la placa ya está registrada
     */
    boolean existsByPlaca(String placa);

    /**
     * Busca un vehículo por placa, solo si su dueño autorizó a ese taller.
     *
     * @param placa    placa del vehículo
     * @param tallerId identificador del taller
     * @return el vehículo, si existe y el taller está autorizado
     */
    @Query("select v from Vehiculo v join v.talleresAutorizados t "
         + "where v.placa = :placa and t.id = :tallerId")
    Optional<Vehiculo> buscarPorPlacaYTaller(@Param("placa") String placa,
                                             @Param("tallerId") Long tallerId);
}
