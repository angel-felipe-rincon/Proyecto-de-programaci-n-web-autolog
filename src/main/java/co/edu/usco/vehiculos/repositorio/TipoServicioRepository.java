
package co.edu.usco.vehiculos.repositorio;

import co.edu.usco.vehiculos.modelo.*;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
/** Acceso al catálogo de tipos de servicio. */
public interface TipoServicioRepository extends JpaRepository<TipoServicio, Long> {
    /**
     * Busca los servicios activos del catálogo.
     *
     * @return los servicios activos
     */
	List<TipoServicio> findByActivoTrue();
    /**
     * Busca los servicios activos que aplican a ciertos tipos de vehículo.
     *
     * @param tipos tipos de vehículo aceptados
     * @return los servicios que cumplen
     */
    List<TipoServicio> findByActivoTrueAndTipoVehiculoIn(Collection<AplicaA> tipos);
}
