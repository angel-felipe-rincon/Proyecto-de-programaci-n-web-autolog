
package co.edu.usco.vehiculos.repositorio;

import co.edu.usco.vehiculos.modelo.*;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
/** Acceso a los usuarios guardados. */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    /**
     * Busca un usuario por su correo.
     *
     * @param correo correo del usuario
     * @return el usuario, si existe
     */
	Optional<Usuario> findByCorreo(String correo);
    /**
     * Busca un usuario por su identificador de Google.
     *
     * @param googleId identificador que entrega Google
     * @return el usuario, si existe
     */
    Optional<Usuario> findByGoogleId(String googleId);
    /**
     * Indica si ya existe un usuario con ese correo.
     *
     * @param correo correo a revisar
     * @return true si ya está registrado
     */
    boolean existsByCorreo(String correo);
    /**
     * Busca los usuarios de un rol.
     *
     * @param rol rol buscado
     * @return los usuarios encontrados
     */
    List<Usuario> findByRol(Rol rol);
    /**
     * Busca los usuarios de un rol que estén en cierto estado.
     *
     * @param rol    rol buscado
     * @param estado estado buscado
     * @return los usuarios encontrados
     */
    List<Usuario> findByRolAndEstado(Rol rol, EstadoUsuario estado);
}
