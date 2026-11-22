package co.edu.usco.vehiculos.controlador;

import co.edu.usco.vehiculos.dto.AuthDto;
import co.edu.usco.vehiculos.modelo.*;
import co.edu.usco.vehiculos.repositorio.UsuarioRepository;
import co.edu.usco.vehiculos.seguridad.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
/** Registro de clientes y talleres, e inicio de sesión. Estos servicios no piden token. */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación")
@SecurityRequirements()
public class AuthController {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    /** Crea el controlador con sus dependencias. */
    public AuthController(UsuarioRepository usuarios, PasswordEncoder encoder, JwtService jwt) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    /**
     * Registra un cliente. Su cuenta queda activa de inmediato.
     *
     * @param r nombre, correo y contraseña
     * @return respuesta vacía con estado 201
     */
    @PostMapping("/registro-cliente")
    @Operation(summary = "Registra un cliente (su cuenta queda activa)")
    public ResponseEntity<Void> registroCliente(@Valid @RequestBody AuthDto.Registro r) {
        return registrar(r, Rol.CLIENTE, EstadoUsuario.ACTIVO);
    }

    /**
     * Registra un taller. Queda pendiente hasta que el administrador lo apruebe.
     *
     * @param r nombre, correo y contraseña
     * @return respuesta vacía con estado 201
     */
    @PostMapping("/registro-taller")
    @Operation(summary = "Registra un taller (queda pendiente hasta que el administrador lo apruebe)")
    public ResponseEntity<Void> registroTaller(@Valid @RequestBody AuthDto.Registro r) {
        return registrar(r, Rol.TALLER, EstadoUsuario.PENDIENTE);
    }
    /**
     * Inicia sesión con correo y contraseña.
     * Un taller pendiente o una cuenta inactiva reciben 403.
     *
     * @param r correo y contraseña
     * @return el token de acceso, el rol y el nombre del usuario
     */
    @PostMapping("/login")
    @Operation(summary = "Inicia sesión y devuelve el token (401 si los datos son incorrectos, 403 si la cuenta no está activa)")
    public AuthDto.Token login(@Valid @RequestBody AuthDto.Login r) {
        Usuario u = usuarios.findByCorreo(r.correo().trim().toLowerCase())
                .filter(x -> x.getClave() != null && encoder.matches(r.clave(), x.getClave()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Credenciales invalidas"));
        if (u.getEstado() == EstadoUsuario.PENDIENTE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Tu cuenta de taller aun esta pendiente de aprobacion");
        }
        if (u.getEstado() != EstadoUsuario.ACTIVO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cuenta inactiva");
        }
        return new AuthDto.Token(jwt.generar(u), u.getRol().name(), u.getNombre());
    }

    private ResponseEntity<Void> registrar(AuthDto.Registro r, Rol rol, EstadoUsuario estado) {
        String correo = r.correo().trim().toLowerCase();
        if (usuarios.existsByCorreo(correo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya esta registrado");
        }
        Usuario u = new Usuario();
        u.setNombre(r.nombre().trim());
        u.setCorreo(correo);
        u.setClave(encoder.encode(r.clave()));
        u.setRol(rol);
        u.setEstado(estado);
        usuarios.save(u);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}