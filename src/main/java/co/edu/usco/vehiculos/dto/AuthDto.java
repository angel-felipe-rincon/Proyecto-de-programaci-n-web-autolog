package co.edu.usco.vehiculos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
/** Datos de registro e inicio de sesión. */
public class AuthDto {
    /** Datos para iniciar sesión. */
    public record Login(@NotBlank @Email String correo, @NotBlank String clave) {}
    /** Datos para registrar un cliente o un taller. La contraseña debe tener entre 8 y 72 caracteres. */
    public record Registro(@NotBlank @Size(max = 120) String nombre,
                           @NotBlank @Email @Size(max = 150) String correo,
                           @NotBlank @Size(min = 8, max = 72) String clave) {}
    /** Respuesta del inicio de sesión: el token de acceso, el rol y el nombre del usuario. */
    public record Token(String token, String rol, String nombre) {}
}