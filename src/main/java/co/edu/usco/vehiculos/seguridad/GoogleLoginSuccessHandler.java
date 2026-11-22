package co.edu.usco.vehiculos.seguridad;

import co.edu.usco.vehiculos.modelo.*;
import co.edu.usco.vehiculos.repositorio.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
/** Termina el ingreso con Google: busca o crea el cliente y genera su token. */
@Component
public class GoogleLoginSuccessHandler implements AuthenticationSuccessHandler {
    private final UsuarioRepository usuarios;
    private final JwtService jwt;
    /** Crea el manejador con sus dependencias. */
    public GoogleLoginSuccessHandler(UsuarioRepository usuarios, JwtService jwt) {
        this.usuarios = usuarios;
        this.jwt = jwt;
    }
    /**
     * Recibe al usuario que entró con Google y lo envía al frontend con su token.
     * Si el correo es nuevo, crea un cliente. Los talleres y el administrador no pueden entrar con Google.
     *
     * @param request        petición
     * @param response       respuesta, se usa para redirigir
     * @param authentication datos que entrega Google
     * @throws IOException si falla la redirección
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User g = (OAuth2User) authentication.getPrincipal();
        String googleId = g.getAttribute("sub");
        String correo = g.getAttribute("email");
        String nombre = g.getAttribute("name");
        Boolean verificado = g.getAttribute("email_verified");

        if (googleId == null || correo == null || !Boolean.TRUE.equals(verificado)) {
            response.sendRedirect("/google-callback.html#error=google");
            return;
        }
        correo = correo.toLowerCase();

        Usuario u = usuarios.findByGoogleId(googleId).orElse(null);
        if (u == null) {
            u = usuarios.findByCorreo(correo).orElse(null);
            if (u != null && u.getRol() != Rol.CLIENTE) {    
                response.sendRedirect("/google-callback.html#error=rol");
                return;
            }
            if (u == null) {
                u = new Usuario();
                u.setNombre(nombre != null ? nombre : correo);
                u.setCorreo(correo);
                u.setRol(Rol.CLIENTE);
                u.setEstado(EstadoUsuario.ACTIVO);
            }
            u.setGoogleId(googleId);
            u = usuarios.save(u);
        }
        if (u.getEstado() != EstadoUsuario.ACTIVO) {
            response.sendRedirect("/google-callback.html#error=inactivo");
            return;
        }
        response.sendRedirect("/google-callback.html#token=" + jwt.generar(u));
    }
}
