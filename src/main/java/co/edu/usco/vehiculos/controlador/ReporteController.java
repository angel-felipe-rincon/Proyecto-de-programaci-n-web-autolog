
package co.edu.usco.vehiculos.controlador;

import co.edu.usco.vehiculos.servicio.PdfServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** Reporte en PDF del historial de un vehículo. */
@RestController
@RequestMapping("/api/cliente")
@Tag(name = "Cliente")
public class ReporteController {
    private final PdfServicio pdf;
    /** Crea el controlador con sus dependencias. */
    public ReporteController(PdfServicio pdf) {
        this.pdf = pdf;
    }
    /**
     * Descarga el historial del vehículo en PDF. Solo lo puede pedir su dueño.
     *
     * @param id     identificador del vehículo
     * @param idioma es, en o pt; si no se envía, es
     * @param auth   usuario autenticado
     * @return el archivo PDF
     */
    @GetMapping("/vehiculos/{id}/historial.pdf")
    @Operation(summary = "Descarga el PDF con los datos y el historial (idioma=es, en o pt)")
    public ResponseEntity<byte[]> historial(@PathVariable Long id,
            @RequestParam(defaultValue = "es") String idioma, Authentication auth) {
        byte[] contenido = pdf.historial(id, auth.getName(), idioma);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"historial-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(contenido);
    }
}