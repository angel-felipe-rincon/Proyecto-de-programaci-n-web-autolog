package co.edu.usco.vehiculos.seguridad;

import co.edu.usco.vehiculos.modelo.*;
import co.edu.usco.vehiculos.repositorio.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
/** Crea la cuenta del administrador la primera vez que arranca la aplicación. */
@Component
public class AdminInicial implements ApplicationRunner {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final String correo;
    private final String clave;
    /** Recibe el correo y la clave del administrador desde application.properties. */
    public AdminInicial(UsuarioRepository usuarios, PasswordEncoder encoder,
                        @Value("${app.admin.correo}") String correo,
                        @Value("${app.admin.clave}") String clave) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.correo = correo;
        this.clave = clave;
    }
    /**
     * Crea el administrador al arrancar, si todavía no existe.
     *
     * @param args argumentos de arranque; no se usan
     */
    @Override
    public void run(ApplicationArguments args) {
        if (!usuarios.existsByCorreo(correo)) {
            Usuario a = new Usuario();
            a.setNombre("Administrador");
            a.setCorreo(correo);
            a.setClave(encoder.encode(clave));
            a.setRol(Rol.ADMIN);
            a.setEstado(EstadoUsuario.ACTIVO);
            usuarios.save(a);
        }
    }
}
