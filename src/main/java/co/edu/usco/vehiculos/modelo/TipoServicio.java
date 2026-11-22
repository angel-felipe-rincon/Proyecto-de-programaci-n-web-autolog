
package co.edu.usco.vehiculos.modelo;

import jakarta.persistence.*;
/** Tipo de servicio del catálogo, como el cambio de aceite. Si es periódico, genera alertas. */
@Entity
@Table(name = "tipo_servicio")
public class TipoServicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(columnDefinition = "text")
    private String descripcion;

    @Column(length = 60)
    private String categoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vehiculo", nullable = false, length = 10)
    private AplicaA tipoVehiculo;

    @Column(nullable = false)
    private boolean periodico;

    @Column(name = "intervalo_km")
    private Integer intervaloKm;

    @Column(name = "margen_aviso_km")
    private Integer margenAvisoKm;

    @Column(nullable = false)
    private boolean activo = true;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}

	public AplicaA getTipoVehiculo() {
		return tipoVehiculo;
	}

	public void setTipoVehiculo(AplicaA tipoVehiculo) {
		this.tipoVehiculo = tipoVehiculo;
	}

	public boolean isPeriodico() {
		return periodico;
	}

	public void setPeriodico(boolean periodico) {
		this.periodico = periodico;
	}

	public Integer getIntervaloKm() {
		return intervaloKm;
	}

	public void setIntervaloKm(Integer intervaloKm) {
		this.intervaloKm = intervaloKm;
	}

	public Integer getMargenAvisoKm() {
		return margenAvisoKm;
	}

	public void setMargenAvisoKm(Integer margenAvisoKm) {
		this.margenAvisoKm = margenAvisoKm;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}
    
}
