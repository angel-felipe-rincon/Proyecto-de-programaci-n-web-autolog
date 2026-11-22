package co.edu.usco.vehiculos.seguridad;

import co.edu.usco.vehiculos.modelo.Usuario;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
/** Crea y valida los tokens JWT. */
@Service
public class JwtService {
    private final SecretKey clave;
    private final long expiracionMs;
    /** Recibe la clave secreta y la duración de los tokens desde application.properties. */
    public JwtService(@Value("${app.jwt.secreto}") String secreto,
                      @Value("${app.jwt.expiracion-ms}") long expiracionMs) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracionMs = expiracionMs;
    }
    /**
     * Genera el token de acceso de un usuario.
     *
     * @param u usuario que inicia sesión
     * @return el token firmado, con su correo, rol y nombre
     */
    public String generar(Usuario u) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(u.getCorreo())
                .claim("rol", u.getRol().name())
                .claim("nombre", u.getNombre())
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expiracionMs))
                .signWith(clave)
                .compact();
    }

    /**
     * Revisa si un token es válido.
     *
     * @param token token recibido
     * @return el correo del usuario, o null si el token no es válido o ya venció
     */
    public String validar(String token) {
        try {
            return Jwts.parser().verifyWith(clave).build()
                    .parseSignedClaims(token).getPayload().getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
