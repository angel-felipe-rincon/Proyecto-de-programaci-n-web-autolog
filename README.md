# AutoLog

Sistema web para llevar el historial de mantenimiento de vehículos (carros y motos). El dueño registra su vehículo y autoriza a los talleres de su confianza; los talleres registran cada servicio que hacen, y el sistema avisa cuando se acerca el próximo mantenimiento según el kilometraje.

Proyecto final de Programación Web, Universidad Surcolombiana (USCO).
Autor: Angel Felipe Rincón Perdomo.

## Funcionalidades

- **Tres roles:** cliente, taller y administrador.
- **Cliente:** registra vehículos, actualiza el kilometraje, autoriza o quita talleres, consulta el historial, ve alertas de mantenimiento y descarga el historial en PDF.
- **Taller:** busca un vehículo por placa, registra mantenimientos y corrige el costo y las observaciones de sus propios registros.
- **Administrador:** aprueba, desactiva o reactiva talleres y administra el catálogo de servicios (periódicos o no).
- **Alertas automáticas:** un trigger de PostgreSQL crea una alerta cuando un vehículo se acerca al próximo servicio periódico.
- **Seguridad:** inicio de sesión con JWT, ingreso con Google para clientes, contraseñas cifradas con BCrypt y HTTPS.
- **Idiomas:** español, inglés y portugués
- **Diseño responsive**

## Tecnologías

| Parte | Tecnología |
|---|---|
| Backend | Java 21, Spring Boot 4.1.2|
| Base de datos | PostgreSQL |
| Autenticación | JWT (jjwt 0.13) y Google OAuth 2.0 |
| Documentación de la API | Swagger / springdoc-openapi 3 |
| Reportes PDF | OpenPDF 3 |
| Frontend | HTML, Bootstrap 5.3, Bootstrap Icons, jQuery 3.7 |

## Requisitos

- JDK 21
- PostgreSQL 14 o superior
- Eclipse

## Instalación y ejecución

1. **Crear la base de datos.** En PostgreSQL, crea una base llamada `vehiculos_db` y ejecuta el script `sql/vehiculos_db.sql`. Crea las tablas y el trigger de alertas.
2. **Revisar la configuración** en `src/main/resources/application.properties`:
   - `spring.datasource.username` y `spring.datasource.password`: usuario y clave de tu PostgreSQL.
3. **Configurar Google (opcional).** Para el ingreso con Google, define estas variables de entorno con las credenciales en Google Cloud Console:
   - `GOOGLE_CLIENT_ID`
   - `GOOGLE_CLIENT_SECRET`

   La URI de redirección autorizada debe ser `https://localhost:8443/login/oauth2/code/google`.
4. **Ejecutar.**
   - En Eclipse: clic derecho sobre el proyecto → **Run As → Spring Boot App**.
5. **Abrir** `https://localhost:8443`.

## Uso rápido

1. Ingresa con el administrador inicial:
   - **Correo:** `admin@vehiculos.local`
   - **Contraseña:** `Admin123*`
2. Crea los servicios del catálogo, por ejemplo "Cambio de aceite", periódico cada 5000 km.
3. Registra un taller desde **Registrarme → Como taller** y apruébalo desde el panel del administrador.
4. Registra un cliente, agrega un vehículo y autoriza al taller.
5. Con el taller, busca la placa y registra mantenimientos.

## Documentación

- **API (Swagger):** `https://localhost:8443/swagger-ui.html`.
- **Javadoc:** abre `doc/index.html` en el navegador.

## Estructura del proyecto

```
vehiculos/
├── doc/                      Javadoc generado
├── sql/vehiculos_db.sql      Tablas, restricciones y trigger de alertas
└── src/main/
    ├── java/co/edu/usco/vehiculos/
    │   ├── controlador/      Endpoints REST
    │   ├── dto/              Datos que entran y salen de la API
    │   ├── modelo/           Entidades JPA y enumeraciones
    │   ├── repositorio/      Acceso a datos
    │   ├── seguridad/        JWT, Google, HTTPS y configuración de Swagger
    │   └── servicio/         Reglas de negocio y PDF
    └── resources/
        ├── application.properties
        ├── keystore.p12      Certificado para HTTPS
        └── static/           Páginas, estilos, scripts, imágenes y traducciones
```