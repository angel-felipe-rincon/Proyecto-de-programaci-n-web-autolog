package co.edu.usco.vehiculos.seguridad;

import co.edu.usco.vehiculos.repositorio.UsuarioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
/** Seguridad de la aplicación: rutas por rol, token JWT e ingreso con Google. */
@Configuration
public class SecurityConfig {
    /**
     * Cifrado de contraseñas con BCrypt.
     *
     * @return el codificador de contraseñas
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    /**
     * Define qué rutas puede usar cada rol.
     * Las páginas del frontend son públicas; la API pide token y sin él responde 401.
     *
     * @param http        configuración de seguridad
     * @param jwt         servicio de tokens
     * @param usuarios    repositorio de usuarios
     * @param googleExito manejador del ingreso con Google
     * @return la cadena de filtros
     * @throws Exception si la configuración falla
     */
    @Bean
    public SecurityFilterChain filtros(HttpSecurity http, JwtService jwt,
                                       UsuarioRepository usuarios,
                                       GoogleLoginSuccessHandler googleExito) throws Exception {
        http
            .csrf(csrf -> csrf.disable())   
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/taller/**").hasRole("TALLER")
                .requestMatchers("/api/cliente/**").hasRole("CLIENTE")
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll())   
            .exceptionHandling(e -> e
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))) 
            .oauth2Login(o -> o.successHandler(googleExito))
            .addFilterBefore(new JwtFilter(jwt, usuarios), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}