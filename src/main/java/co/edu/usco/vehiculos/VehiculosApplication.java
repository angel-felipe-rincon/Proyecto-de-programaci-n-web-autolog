package co.edu.usco.vehiculos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/** Punto de entrada de AutoLog, el sistema de historial de mantenimiento de vehículos. */
@SpringBootApplication
public class VehiculosApplication {
    /**
     * Arranca la aplicación.
     *
     * @param args argumentos de la línea de comandos
     */
	public static void main(String[] args) {
		SpringApplication.run(VehiculosApplication.class, args);
	}

}
