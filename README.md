# Huellitas Conectadas

Plataforma web académica para conectar a personas interesadas en adoptar animales con albergues, refugios y veterinarias.

## Funcionalidad de la T3

- **Adoptante.** Consulta el catálogo paginado de mascotas disponibles, filtra por especie, abre la galería de la ficha y sigue el estado de sus solicitudes.
- **Organización.** Publica mascotas con fotos, administra sus fichas y revisa las solicitudes de adopción recibidas. Una organización activa solo puede gestionar sus propios recursos.
- **Administración.** Consulta, edita y activa o desactiva perfiles de organizaciones.
- **Seguridad.** Spring autentica con JWT. Angular usa una cookie de sesión, recupera el rol al recargar y protege las rutas con Guards.
- **Documentación y pruebas.** Swagger UI permite consultar OpenAPI y ejecutar peticiones. GitHub Actions compila y prueba Angular y Spring.

[Aplicación web](https://hc.itechk.us) · [Swagger UI](https://spring.itechk.us/swagger-ui.html) · [OpenAPI JSON](https://spring.itechk.us/v3/api-docs)

## Tecnologías

### Backend

- Java 25 y Spring Boot 4.1.1.
- Maven Wrapper.
- MySQL 8, Spring Data JPA e Hibernate.
- Spring Security, JWT y BCrypt.
- Jakarta Validation.
- AWS SDK para Java, conectado a Garage con protocolo S3.
- springdoc-openapi 3.2.0 para documentación OpenAPI y Swagger UI.

### Frontend

- Angular 21.2 y TypeScript.
- Reactive Forms para inicio de sesión y publicación de mascotas.
- Signal Forms para los formularios de registro.
- Angular HttpClient para consumir la API.
- Servicios y modelos separados de los componentes; Angular Router y Bootstrap 5.
- CSS adaptable para catálogo y detalle.

## Endpoints principales

Todas las rutas de estas tablas usan el prefijo `/api/v1`. Angular accede a los endpoints protegidos con la cookie de sesión. Swagger y los clientes externos también pueden enviar:

```http
Authorization: Bearer <JWT>
```

### Autenticación

| Método | Endpoint | Acceso | Descripción |
|---|---|---|---|
| POST | `/auth/register` | Público | Registra una cuenta de adoptante. |
| POST | `/auth/register/organizacion` | Público | Registra una cuenta y el perfil de una organización. |
| POST | `/auth/login` | Público | Devuelve el JWT, correo y rol para clientes Bearer. |
| POST | `/auth/login/browser` | Público | Devuelve el correo y rol y entrega el JWT en una cookie. Requiere `X-Huellitas-Request: 1`. |
| GET | `/auth/session` | Autenticado | Recupera el correo y el rol de la sesión válida. |
| POST | `/auth/logout` | Público | Expira la cookie y devuelve `204 No Content`. Requiere `X-Huellitas-Request: 1`. |

### Organizaciones

| Método | Endpoint | Acceso | Descripción |
|---|---|---|---|
| GET | `/organizaciones` | Autenticado | Lista organizaciones. |
| GET | `/organizaciones/{id}` | Autenticado | Consulta una organización. |
| POST | `/organizaciones` | ADMIN | Crea un perfil de organización directamente (el registro de cuentas usa `/auth/register/organizacion`). |
| PUT | `/organizaciones/{id}` | ADMIN | Actualiza un perfil. |
| DELETE | `/organizaciones/{id}` | ADMIN | Elimina un perfil mediante la API; la interfaz no ofrece este botón. |
| PATCH | `/organizaciones/{id}/activo` | ADMIN | Activa o desactiva un perfil con `{ "activo": true }` o `{ "activo": false }`. |

El registro desde Angular crea el perfil inicialmente inactivo. La organización debe ser activada por un administrador antes de publicar o gestionar mascotas.

### Mascotas y fotos

| Método | Endpoint | Acceso | Descripción |
|---|---|---|---|
| GET | `/animales` | Autenticado | Lista animales. |
| GET | `/animales/catalogo` | Autenticado | Lista las mascotas disponibles con filtro por especie y paginación. |
| GET | `/animales/mios` | ORGANIZACION | Lista las publicaciones de la organización autenticada. |
| GET | `/animales/{id}` | Autenticado | Consulta una mascota. |
| GET | `/animales/{id}/fotos` | Autenticado | Lista las fotos para la galería de la ficha. |
| POST | `/animales/publicar` | ORGANIZACION | Publica una mascota y sube sus fotos usando `multipart/form-data`. |
| PUT | `/animales/{id}` | ORGANIZACION | Actualiza una mascota propia. |
| DELETE | `/animales/{id}` | ORGANIZACION | Elimina una mascota propia. |
| GET | `/fotos/{id}` | Público | Devuelve una foto almacenada en Garage. |

Para `POST /animales/publicar`, el formulario debe enviar una parte `animal` con JSON y entre 2 y 8 partes repetidas llamadas `fotos`. Se aceptan JPEG, PNG y WebP, con un máximo de 5 MB por archivo. La respuesta incluye la mascota creada y las URLs de las fotos. La ruta antigua `POST /animales` está bloqueada; se debe usar `POST /animales/publicar`.

El backend comprueba que la cuenta tenga una organización activa. Al actualizar o eliminar, verifica que la mascota pertenezca a esa organización.

### Catálogo paginado

```http
GET /api/v1/animales/catalogo?page=0&size=12&especie=TODAS
```

- `page` empieza en 0. `size` admite de 1 a 50 registros y su valor predeterminado es 12.
- `especie` admite `TODAS`, `PERRO`, `GATO` y `OTRO`. El filtro se aplica antes de paginar y los resultados se ordenan por identificador descendente.
- La respuesta contiene `content`, `number`, `size`, `totalElements` y `totalPages`. Angular usa estos datos para mostrar el total y navegar con **Anterior** y **Siguiente**. Al cambiar el filtro, vuelve a la primera página.

### Solicitudes de adopción

| Método | Endpoint | Acceso | Descripción |
|---|---|---|---|
| POST | `/solicitudes-adopcion/animales/{animalId}` | ADOPTANTE | Envía una solicitud con información sobre vivienda, otras mascotas y un mensaje opcional. |
| GET | `/solicitudes-adopcion/mias` | ADOPTANTE | Consulta el historial de solicitudes propias. |
| GET | `/solicitudes-adopcion/organizacion` | ORGANIZACION | Consulta las solicitudes recibidas para las mascotas de la organización. |
| PATCH | `/solicitudes-adopcion/{id}/estado` | ORGANIZACION | Aprueba o rechaza una solicitud pendiente de una publicación propia. |

El servidor exige una mascota disponible, una organización activa y ausencia de solicitudes pendientes duplicadas del mismo adoptante para esa mascota. Al aprobar una solicitud, la mascota pasa a `ADOPTADO` y las demás solicitudes pendientes para ella se rechazan.

## Swagger UI y OpenAPI

**Swagger UI es una interfaz web para consultar y probar los endpoints del backend.** Muestra las rutas,
métodos HTTP, datos de entrada y respuestas de la API. **OpenAPI** es la especificación que describe
ese contrato; springdoc la genera a partir de los controladores y DTO de Spring.
Esto facilita revisar la integración y demostrar el funcionamiento del proyecto sin depender de Angular o EchoAPI.

La API incluye `springdoc-openapi-starter-webmvc-ui` 3.2.0, compatible con Spring Boot 4.1.x.
Con el backend iniciado en el puerto 8080:

- Interfaz local: <http://localhost:8080/swagger-ui.html>.
- Interfaz por el túnel del backend: <https://spring.itechk.us/swagger-ui.html>.
- Especificación JSON: `/v3/api-docs`; YAML: `/v3/api-docs.yaml`.

Estas direcciones pertenecen a Spring, no al dominio del frontend `hc.itechk.us`.
Swagger usa el origen desde el que se abre, por lo que funciona tanto localmente como por HTTPS.

### Probar endpoints con JWT

1. Abre **Autenticación → POST /api/v1/auth/login → Try it out**.
2. Completa `email` y `pass` con una cuenta existente y pulsa **Execute**.
3. Copia el campo `token` de la respuesta, pulsa **Authorize** y pega solo el JWT, sin `Bearer `.
4. Ejecuta `GET /api/v1/animales/catalogo` con `page=0` y `size=12`. Una sesión válida devuelve `200 OK` y los resultados paginados.
5. Para comprobar permisos, usa una cuenta de adoptante y ejecuta `GET /api/v1/animales/mios`. El resultado esperado es `403 Forbidden`. Una petición al catálogo sin JWT ni cookie válida devuelve `401 Unauthorized`.

Para **POST /api/v1/animales/publicar**, introduce el objeto `animal` como JSON y selecciona
entre 2 y 8 archivos en `fotos`. La documentación define `application/json` para la parte `animal`.
Ejemplo de objeto:

```json
{"nombre":"Luna","especie":"Perro","raza":"Mestiza","edadMeses":24,"sexo":"HEMBRA","tamanio":"MEDIANO","descripcion":"Busca una familia.","estado":"DISPONIBLE"}
```

La documentación es pública; los permisos de la API siguen siendo los de Spring Security.
Las acciones de **Execute** utilizan la base de datos y el almacenamiento configurados:
crear, editar o eliminar desde Swagger realiza la operación real.
La autorización no se configura para persistir entre recargas de Swagger.

### Comprobar la integración

```bash
./mvnw -DskipTests compile
./mvnw -Dtest=OpenApiDocumentationTest test
```

La prueba usa H2 en memoria y credenciales ficticias, sin conectarse a MySQL ni subir archivos a Garage.
La suite verifica OpenAPI, Swagger UI, autenticación con cookie y Bearer JWT, protección de operaciones con cookie, códigos `401` y `403`, paginación y reglas del ciclo de adopción.

## Garage S3

El backend necesita estas variables de entorno para conectarse al almacenamiento:

- `GARAGE_S3_ENDPOINT`
- `GARAGE_S3_REGION`
- `GARAGE_S3_BUCKET`
- `GARAGE_S3_ACCESS_KEY_ID`
- `GARAGE_S3_SECRET_KEY`

No publiques sus valores, contraseñas ni tokens en GitHub. En el miniserver, el archivo de configuración local se carga antes de iniciar Spring:

```bash
set -a
source ~/.config/huellitas/garage.env
set +a
./mvnw spring-boot:run
```

La aplicación también admite `app.api.public-url` para construir las URLs públicas de las fotos; por defecto usa `https://spring.itechk.us`.

## Seguridad y configuración

- El registro y el inicio de sesión son públicos. La recuperación de sesión exige un JWT válido.
- Los endpoints de negocio requieren autenticación, excepto la lectura pública de fotos; Swagger UI y la especificación OpenAPI también son públicos.
- La publicación y gestión de mascotas requieren rol `ORGANIZACION`; el servicio valida que la organización esté activa y sea propietaria de la mascota.
- Crear, editar o eliminar organizaciones directamente por la API, así como cambiar su estado, requiere autoridad `ADMIN`.
- Las fotos tienen una ruta de lectura pública para que el navegador pueda mostrarlas.
- El backend habilita CORS para los orígenes configurados en `SecurityConfig`. Si cambia la dirección o el puerto del frontend, hay que actualizar la lista de orígenes permitidos.
- El frontend apunta a `https://spring.itechk.us/api/v1`. Si cambia el origen de la API, actualiza las URLs de los servicios en `frontend/src/app/core/` y el origen reconocido por el interceptor de credenciales en `frontend/src/app/app.config.ts`.

Spring entrega el JWT de la sesión web en la cookie `__Host-huellitas-session` con `HttpOnly`, `Secure` y `SameSite=Lax`. JavaScript recibe el correo y el rol. Al recargar, Angular consulta `/auth/session` para restaurar el estado. Al cerrar sesión, Spring expira la cookie y Angular vuelve al login. El JWT firmado conserva su vigencia hasta la expiración.

Angular envía las solicitudes a la API con credenciales y añade `X-Huellitas-Request: 1` a las operaciones que modifican datos. Spring exige esa cabecera para las operaciones con cookie y restringe los orígenes mediante CORS. Las contraseñas se almacenan como hashes BCrypt.

## Estructura del repositorio

```text
.
├── frontend/                 # Aplicación Angular
├── src/main/java/            # Backend Spring Boot
│   └── com/hc/application/
│       ├── config/
│       ├── controller/
│       ├── dto/
│       ├── entity/
│       ├── exception/
│       ├── repository/
│       ├── security/
│       └── service/
├── src/main/resources/       # Configuración de Spring
├── docs/                     # Documentación
├── mvnw
└── pom.xml
```

## Requisitos

- Java JDK 25.
- MySQL 8.
- Node.js 24 y npm 11 para el frontend.
- Garage configurado como almacén S3 para publicar y recuperar fotos.
- Git.

## Ejecución en desarrollo

### Backend

Usa [la plantilla de configuración](src/main/resources/templates/application-example.properties) como base para un `src/main/resources/application.properties` local. Configura MySQL, la clave de firma Base64 en `app.jwt.secret` y la vigencia en milisegundos en `app.jwt.expiration`. Carga las variables de Garage antes de iniciar Spring. Mantén los valores de configuración sensibles fuera del repositorio.

Desde la raíz del repositorio:

```bash
./mvnw spring-boot:run
```

Spring Boot escucha por defecto en `http://localhost:8080`.

### Frontend

En otra terminal:

```bash
cd frontend
npm ci
npm start -- --host 0.0.0.0
```

Angular escucha en el puerto 4200 para desarrollo. Para probar el flujo completo de sesión por cookie, abre el frontend y la API mediante HTTPS en el mismo sitio, como `hc.itechk.us` y `spring.itechk.us`. Si usas un entorno distinto, configura sus orígenes en Angular y Spring. Para generar la compilación del frontend, ejecuta `npm run build`.

## Flujo de uso

1. Registra una cuenta de organización y espera a que una cuenta ADMIN active su perfil.
2. Inicia sesión con la organización activa: en «Mis mascotas» puedes publicar con al menos dos fotos, editar una publicación y eliminarla.
3. Registra o inicia sesión con una cuenta de adoptante.
4. Explora el catálogo paginado, selecciona una ficha y envía una solicitud de adopción.
5. Entra como organización y aprueba o rechaza la solicitud desde su bandeja.
6. Entra como adoptante y verifica el resultado en **Mis solicitudes**.

## Pruebas y evidencias

```bash
./mvnw test
cd frontend
npm ci
npm run build
npm test -- --watch=false
```

[GitHub Actions](https://github.com/alm933/huellitas-conectadas/actions) ejecuta la compilación y las pruebas de Angular y Spring. La [verificación del cambio de sesión segura](https://github.com/alm933/huellitas-conectadas/actions/runs/42683339054) finalizó correctamente.

En el despliegue académico se verificó que la sesión de adoptante se recupera al recargar, que el catálogo cambia de página y que el cierre de sesión redirige al login. En Swagger se observó `200` al consultar el catálogo con sesión, `401` al consultarlo sin sesión y `403` al intentar acceder como adoptante a una ruta de organización.

## Proyecto académico

- Curso: Soluciones Web y Aplicaciones Distribuidas.
- Entrega: T3, grupo 02.
- Autor: Alejandro Leon.
