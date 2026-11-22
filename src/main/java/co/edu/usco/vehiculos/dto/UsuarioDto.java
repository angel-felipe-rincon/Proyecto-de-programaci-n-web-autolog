
package co.edu.usco.vehiculos.dto;

import co.edu.usco.vehiculos.modelo.*;
/** Datos de usuarios que devuelve la API. */
public class UsuarioDto {
    /** Datos de un usuario para el administrador, sin la contraseña. */
    public record Resumen(Long id, String nombre, String correo, Rol rol, EstadoUsuario estado) {
        /**
         * Convierte un usuario en resumen.
         *
         * @param u usuario
         * @return sus datos sin la contraseña
         */
        public static Resumen de(Usuario u) {
            return new Resumen(u.getId(), u.getNombre(), u.getCorreo(), u.getRol(), u.getEstado());
        }
    }

    /** Datos de un taller que puede ver un cliente: solo su identificador y su nombre. */
    public record Publico(Long id, String nombre) {
        /**
         * Toma solo el identificador y el nombre del taller.
         *
         * @param u taller
         * @return los datos públicos del taller
         */
        public static Publico de(Usuario u) {
            return new Publico(u.getId(), u.getNombre());
        }
    }
}