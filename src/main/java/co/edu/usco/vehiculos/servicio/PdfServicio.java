
package co.edu.usco.vehiculos.servicio;

import co.edu.usco.vehiculos.modelo.Mantenimiento;
import co.edu.usco.vehiculos.modelo.TipoVehiculo;
import co.edu.usco.vehiculos.modelo.Vehiculo;
import co.edu.usco.vehiculos.repositorio.MantenimientoRepository;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import org.openpdf.text.Document;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Genera el PDF del historial de un vehículo. */
@Service
public class PdfServicio {
    /**
     * Textos del PDF en cada idioma. Los tres siguen el mismo orden:
     * título, datos del vehículo, columnas de la tabla, mensaje sin historial y fecha de generación.
     */
    private static final String[] ES = {"Historial de mantenimiento", "Placa", "Marca", "Modelo", "A\u00f1o",
            "Tipo", "Kilometraje", "Propietario", "Fecha", "Servicio", "Km", "Taller", "Costo",
            "Observaciones", "A\u00fan no hay mantenimientos registrados.", "Generado el"};
    private static final String[] EN = {"Maintenance history", "Plate", "Make", "Model", "Year",
            "Type", "Mileage", "Owner", "Date", "Service", "Km", "Workshop", "Cost",
            "Notes", "No maintenance records yet.", "Generated on"};
    private static final String[] PT = {"Hist\u00f3rico de manuten\u00e7\u00e3o", "Placa", "Marca", "Modelo", "Ano",
            "Tipo", "Quilometragem", "Propriet\u00e1rio", "Data", "Servi\u00e7o", "Km", "Oficina", "Custo",
            "Observa\u00e7\u00f5es", "Ainda n\u00e3o h\u00e1 manuten\u00e7\u00f5es registradas.", "Gerado em"};

    private final VehiculoServicio vehiculos;
    private final MantenimientoRepository mantenimientos;
    /** Crea el servicio con sus dependencias. */
    public PdfServicio(VehiculoServicio vehiculos, MantenimientoRepository mantenimientos) {
        this.vehiculos = vehiculos;
        this.mantenimientos = mantenimientos;
    }
    /**
     * Genera el PDF con los datos del vehículo y su historial. Solo lo puede pedir el dueño.
     *
     * @param vehiculoId identificador del vehículo
     * @param correo     correo del cliente
     * @param idioma     en para inglés, pt para portugués; cualquier otro valor lo genera en español
     * @return el contenido del PDF
     */
    @Transactional(readOnly = true)
    public byte[] historial(Long vehiculoId, String correo, String idioma) {
        Vehiculo v = vehiculos.propio(vehiculoId, correo);   
        List<Mantenimiento> lista = mantenimientos.findByVehiculoIdOrderByFechaDescIdDesc(vehiculoId);

        
        String id = idioma == null ? "es" : idioma.toLowerCase();
        boolean en = id.equals("en");
        boolean pt = id.equals("pt");
        String[] x = en ? EN : pt ? PT : ES;
        Locale loc = en ? Locale.US : pt ? Locale.forLanguageTag("pt-BR") : Locale.forLanguageTag("es-CO");
        DateTimeFormatter fecha = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(loc);
        NumberFormat km = NumberFormat.getIntegerInstance(loc);
        NumberFormat dinero = NumberFormat.getCurrencyInstance(loc);
        dinero.setCurrency(Currency.getInstance("COP"));
        dinero.setMaximumFractionDigits(0);
        
        String tipo = v.getTipo() == TipoVehiculo.MOTO ? (en ? "Motorcycle" : "Moto") : (en ? "Car" : "Carro");

        Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font negrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);

        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        try {
            Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(doc, salida);
            doc.open();
            doc.add(new Paragraph(x[0], titulo));
            doc.add(new Paragraph(x[15] + " " + LocalDate.now().format(fecha), normal));
            doc.add(new Paragraph(" ", normal));
            doc.add(new Paragraph(x[1] + ": " + v.getPlaca(), negrita));
            doc.add(new Paragraph(x[2] + ": " + v.getMarca() + "    " + x[3] + ": " + v.getModelo()
                    + "    " + x[4] + ": " + v.getAnio(), normal));
            doc.add(new Paragraph(x[5] + ": " + tipo + "    " + x[6] + ": " + km.format(v.getKilometraje()) + " km", normal));
            doc.add(new Paragraph(x[7] + ": " + v.getCliente().getNombre(), normal));
            doc.add(new Paragraph(" ", normal));

            if (lista.isEmpty()) {
                doc.add(new Paragraph(x[14], normal));
            } else {
                PdfPTable tabla = new PdfPTable(6);
                tabla.setWidthPercentage(100);
                for (int i = 8; i <= 13; i++) {
                    tabla.addCell(celda(x[i], negrita));
                }
                tabla.setHeaderRows(1);
                for (Mantenimiento m : lista) {
                    tabla.addCell(celda(m.getFecha().format(fecha), normal));
                    tabla.addCell(celda(m.getTipoServicio().getNombre(), normal));
                    tabla.addCell(celda(km.format(m.getKilometraje()), normal));
                    tabla.addCell(celda(m.getTaller().getNombre(), normal));
                    tabla.addCell(celda(dinero.format(m.getCosto()), normal));
                    tabla.addCell(celda(m.getObservaciones() == null ? "" : m.getObservaciones(), normal));
                }
                doc.add(tabla);
            }
            doc.close();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el PDF", e);
        }
        return salida.toByteArray();
    }

    private static PdfPCell celda(String texto, Font fuente) {
        PdfPCell c = new PdfPCell(new Phrase(texto, fuente));
        c.setPadding(4);
        return c;
    }
}
