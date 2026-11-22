package co.edu.usco.vehiculos.seguridad;

import co.edu.usco.vehiculos.modelo.EstadoUsuario;
import co.edu.usco.vehiculos.repositorio.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro que revisa el token en cada petición.
 * No es un componente de Spring: lo crea SecurityConfig, para que no se registre dos veces.
 */
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final UsuarioRepository usuarios;
    /** Crea el filtro con el servicio de tokens y el repositorio de usuarios. */
    public JwtFilter(JwtService jwt, UsuarioRepository usuarios) {
        this.jwt = jwt;
        this.usuarios = usuarios;
    }
    /**
     * Lee el token del encabezado Authorization y, si es válido y la cuenta sigue activa, autentica al usuario.
     *
     * @param request  petición
     * @param response respuesta
     * @param chain    resto de filtros
     * @throws ServletException si falla un filtro siguiente
     * @throws IOException si falla la lectura o la escritura
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String cabecera = request.getHeader("Authorization");
        if (cabecera != null && cabecera.startsWith("Bearer ")) {
            String correo = jwt.validar(cabecera.substring(7));
            if (correo != null) {
                usuarios.findByCorreo(correo)
                        .filter(u -> u.getEstado() == EstadoUsuario.ACTIVO)
                        .ifPresent(u -> {
                            var auth = new UsernamePasswordAuthenticationToken(
                                    u.getCorreo(), null,
                                    List.of(new SimpleGrantedAuthority("ROLE_" + u.getRol().name())));
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        });
            }
        }
        chain.doFilter(request, response);
    }
}
