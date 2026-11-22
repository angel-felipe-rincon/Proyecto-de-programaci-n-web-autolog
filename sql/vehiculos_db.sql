-- TABLAS
CREATE TABLE usuario (
    id        BIGSERIAL PRIMARY KEY,
    nombre    VARCHAR(120) NOT NULL,
    correo    VARCHAR(150) NOT NULL UNIQUE,
    clave     VARCHAR(100),                       -- vacía si el cliente entra con Google
    rol       VARCHAR(20)  NOT NULL CHECK (rol IN ('CLIENTE','TALLER','ADMIN')),
    estado    VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO'
              CHECK (estado IN ('ACTIVO','PENDIENTE','INACTIVO')),
    google_id VARCHAR(100) UNIQUE
);

CREATE TABLE vehiculo (
    id           BIGSERIAL PRIMARY KEY,
    placa        VARCHAR(10) NOT NULL UNIQUE,
    marca        VARCHAR(60) NOT NULL,
    modelo       VARCHAR(60) NOT NULL,
    anio         INT         NOT NULL,
    tipo         VARCHAR(10) NOT NULL CHECK (tipo IN ('MOTO','CARRO')),
    kilometraje  INT         NOT NULL CHECK (kilometraje >= 0),
    cliente_id   BIGINT      NOT NULL REFERENCES usuario(id)
);

-- Qué talleres puede ver cada vehículo
CREATE TABLE autorizacion_taller (
    vehiculo_id BIGINT NOT NULL REFERENCES vehiculo(id) ON DELETE CASCADE,
    taller_id   BIGINT NOT NULL REFERENCES usuario(id),
    PRIMARY KEY (vehiculo_id, taller_id)
);

CREATE TABLE tipo_servicio (
    id              BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL,
    descripcion     TEXT,
    categoria       VARCHAR(60),
    tipo_vehiculo   VARCHAR(10) NOT NULL CHECK (tipo_vehiculo IN ('MOTO','CARRO','AMBOS')),
    periodico       BOOLEAN NOT NULL DEFAULT FALSE,
    intervalo_km    INT,
    margen_aviso_km INT,
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    CHECK (periodico = FALSE OR (intervalo_km > 0 AND margen_aviso_km >= 0))
);

CREATE TABLE mantenimiento (
    id               BIGSERIAL PRIMARY KEY,
    vehiculo_id      BIGINT NOT NULL REFERENCES vehiculo(id),
    taller_id        BIGINT NOT NULL REFERENCES usuario(id),
    tipo_servicio_id BIGINT NOT NULL REFERENCES tipo_servicio(id),
    fecha            DATE   NOT NULL,
    kilometraje      INT    NOT NULL CHECK (kilometraje >= 0),
    costo            NUMERIC(12,2) NOT NULL CHECK (costo >= 0),
    observaciones    TEXT
);

CREATE TABLE alerta (
    id               BIGSERIAL PRIMARY KEY,
    vehiculo_id      BIGINT NOT NULL REFERENCES vehiculo(id) ON DELETE CASCADE,
    tipo_servicio_id BIGINT NOT NULL REFERENCES tipo_servicio(id),
    mensaje          VARCHAR(300) NOT NULL,
    fecha_creacion   TIMESTAMP NOT NULL DEFAULT now(),
    activa           BOOLEAN NOT NULL DEFAULT TRUE
);

-- Para que no haya dos alertas activas del mismo servicio en el mismo vehículo
CREATE UNIQUE INDEX uq_alerta_activa
    ON alerta (vehiculo_id, tipo_servicio_id) WHERE activa;

-- TRIGGER DE ALERTAS POR KILOMETRAJE
CREATE OR REPLACE FUNCTION fn_generar_alertas() RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO alerta (vehiculo_id, tipo_servicio_id, mensaje)
    SELECT NEW.id, ts.id,
           'Se acerca el servicio "' || ts.nombre || '": toca a los '
           || (u.ultimo_km + ts.intervalo_km) || ' km'
    FROM tipo_servicio ts
    JOIN LATERAL (
        SELECT MAX(m.kilometraje) AS ultimo_km
        FROM mantenimiento m
        WHERE m.vehiculo_id = NEW.id AND m.tipo_servicio_id = ts.id
    ) u ON u.ultimo_km IS NOT NULL
    WHERE ts.periodico AND ts.activo
      AND (ts.tipo_vehiculo = 'AMBOS' OR ts.tipo_vehiculo = NEW.tipo)
      AND NEW.kilometraje >= (u.ultimo_km + ts.intervalo_km - ts.margen_aviso_km)
      AND NOT EXISTS (SELECT 1 FROM alerta a
                      WHERE a.vehiculo_id = NEW.id
                        AND a.tipo_servicio_id = ts.id AND a.activa);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_alertas
AFTER UPDATE OF kilometraje ON vehiculo
FOR EACH ROW
WHEN (NEW.kilometraje IS DISTINCT FROM OLD.kilometraje)
EXECUTE FUNCTION fn_generar_alertas();