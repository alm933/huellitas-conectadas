# Huellitas Conectadas

Plataforma web académica para conectar a personas interesadas en adoptar animales con albergues, refugios y veterinarias.

## Estado actual

El repositorio contiene un backend REST con Spring Boot y un frontend Angular en `frontend/`. La rama de trabajo actual reúne el flujo básico para registrar organizaciones y adoptantes, moderar organizaciones, publicar mascotas con fotos y permitir que adoptantes consulten las publicaciones.

### Funcionalidad implementada

- Registro e inicio de sesión con JWT y control de acceso mediante Spring Security.
- Panel de administración para consultar, editar y activar o desactivar organizaciones.
- Vista de organización para consultar sus mascotas, publicar con entre 2 y 8 fotos, editar datos y estado, y eliminar publicaciones.
- Almacenamiento de fotos en Garage mediante su API compatible con S3; el backend conserva las claves y metadatos asociados.
- Catálogo para adoptantes que muestra mascotas con estado `DISPONIBLE`.
- Vista de detalle con foto principal, edad, sexo, tamaño, historia y organización responsable.
- Diseño adaptable a pantallas de celular y escritorio.
- Validación de propiedad para que una organización gestione únicamente sus mascotas.
- Rutas Angular por rol, con protección de acceso y retorno al login cuando no existe sesión.

### Pendiente

- Solicitudes de adopción y su seguimiento.
- Persistencia de favoritos del adoptante; el corazón actual es solo visual y se reinicia al salir de la vista.
- Galería de fotos en la vista de detalle; por ahora se muestra la foto principal.
- Despliegue de producción del frontend. Angular se ejecuta actualmente con su servidor de desarrollo.
- Pruebas automatizadas de los flujos principales y documentación final de despliegue.

## Tecnologías

### Backend

- Java 25 y Spring Boot 4.1.1.
- Maven Wrapper.
- MySQL 8, Spring Data JPA e Hibernate.
- Spring Security, JWT y BCrypt.
- Jakarta Validation.
- AWS SDK para Java, conectado a Garage con protocolo S3.

### Frontend

- Angular 21.2 y TypeScript.
- Reactive Forms para inicio de sesión y publicación de mascotas.
- Signal Forms para los formularios de registro.
- Angular HttpClient para consumir la API.
- Servicios y modelos separados de los componentes; Angular Router y Bootstrap 5.
- CSS adaptable para catálogo y detalle.

## Endpoints principales

Todas las rutas usan el prefijo `/api/v1`, salvo indicación contraria. Las rutas protegidas requieren el encabezado:

```http
Authorization: Bearer <JWT>
```

### Autenticación

| Método | Endpoint | Acceso | Descripción |
|---|---|---|---|
| POST | `/auth/register` | Público | Registra una cuenta de adoptante. |
| POST | `/auth/register/organizacion` | Público | Registra una cuenta y el perfil de una organización. |
| POST | `/auth/login` | Público | Inicia sesión y devuelve el JWT, correo y rol. |

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
| GET | `/animales` | Autenticado | Lista animales. El catálogo Angular filtra los que tienen estado `DISPONIBLE`. |
| GET | `/animales/mios` | ORGANIZACION | Lista las publicaciones de la organización autenticada. |
| GET | `/animales/{id}` | Autenticado | Consulta una mascota. |
| POST | `/animales/publicar` | ORGANIZACION | Publica una mascota y sube sus fotos usando `multipart/form-data`. |
| PUT | `/animales/{id}` | ORGANIZACION | Actualiza una mascota propia. |
| DELETE | `/animales/{id}` | ORGANIZACION | Elimina una mascota propia. |
| GET | `/fotos/{id}` | Público | Devuelve una foto almacenada en Garage. |

Para `POST /animales/publicar`, el formulario debe enviar una parte `animal` con JSON y entre 2 y 8 partes repetidas llamadas `fotos`. Se aceptan JPEG, PNG y WebP, con un máximo de 5 MB por archivo. La respuesta incluye la mascota creada y las URLs de las fotos. La ruta antigua `POST /animales` está bloqueada; se debe usar `POST /animales/publicar`.

El backend comprueba que la cuenta tenga una organización activa. Al actualizar o eliminar, verifica que la mascota pertenezca a esa organización.

## Swagger UI y OpenAPI

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
4. Prueba las operaciones del rol de esa cuenta. Un adoptante no puede publicar mascotas;
   una organización solo puede modificar sus propias publicaciones.

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
Verifica OpenAPI, Swagger UI, JWT, el tipo JSON del multipart y el rechazo de peticiones anónimas
a endpoints protegidos. GitHub Actions ejecuta esta misma prueba.

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

- Las rutas de autenticación permiten acceso sin JWT.
- El resto de la API requiere autenticación.
- La publicación y gestión de mascotas requieren rol `ORGANIZACION`; el servicio valida que la organización esté activa y sea propietaria de la mascota.
- Crear, editar o eliminar organizaciones directamente por la API, así como cambiar su estado, requiere autoridad `ADMIN`.
- Las fotos tienen una ruta de lectura pública para que el navegador pueda mostrarlas.
- El backend habilita CORS para los orígenes configurados en `SecurityConfig`. Si cambia la dirección o el puerto del frontend, hay que actualizar la lista de orígenes permitidos.
- El frontend apunta actualmente a `https://spring.itechk.us/api/v1`; ajusta las URLs base de los servicios en `frontend/src/app/core/` si cambias de entorno.

El JWT se conserva en `sessionStorage` de la pestaña: recargar no cierra la sesión; el botón «Cerrar sesión» lo borra. No se guardan contraseñas. Al cerrar la pestaña termina esa sesión.

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

Configura la conexión de MySQL en `src/main/resources/application.properties` y carga las variables de Garage en el entorno. No publiques credenciales.

Desde la raíz del repositorio:

```bash
./mvnw spring-boot:run
```

Spring Boot escucha por defecto en `http://localhost:8080`.

### Frontend

En otra terminal:

```bash
cd frontend
npm install
npm run build
npm start -- --host 0.0.0.0
```

Angular escucha por defecto en el puerto 4200. Abre `http://localhost:4200` desde el equipo que accede al servidor, o configura el reenvío del puerto 4200 en VS Code Remote SSH. Mantén activa la terminal de `npm start`. Este servidor es solo para desarrollo y pruebas.

## Flujo de uso

1. Registra una cuenta de organización y espera a que una cuenta ADMIN active su perfil.
2. Inicia sesión con la organización activa: en «Mis mascotas» puedes publicar con al menos dos fotos, editar una publicación y eliminarla.
3. Registra o inicia sesión con una cuenta de adoptante.
4. El catálogo consulta `GET /api/v1/animales` con el JWT y muestra las mascotas disponibles.
5. Selecciona una tarjeta para abrir `/adoptante/mascotas/{id}` y ver la foto principal y los datos de la mascota.

## Verificación realizada

- El frontend completó `npm run build` y `npm test -- --watch=false` en la rama T2 (3 pruebas). El umbral de advertencia inicial es 750 kB por la hoja de estilos de Bootstrap.
- El backend necesita Maven Central o un caché Maven local con Spring Boot 4.1.1 para compilarse. Antes de desplegar esta rama, ejecutar `./mvnw -DskipTests compile` en el miniserver.
- Se publicó una mascota con dos imágenes mediante `POST /api/v1/animales/publicar`; el servidor respondió `201 Created` y devolvió las URLs de fotos.
- La conexión a Garage se comprobó desde Cyberduck mediante un túnel SSH local.

## Proyecto académico

- Curso: Soluciones Web y Aplicaciones Distribuidas.
- Proyecto: Huellitas Conectadas.
- Autor: Alejandro Leon.
