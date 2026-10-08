# Huellitas Conectadas

Plataforma web académica para conectar a personas interesadas en adoptar animales con albergues, refugios y veterinarias.

## Estado del proyecto

El proyecto tiene un backend REST en Spring Boot y un frontend independiente en Angular, dentro de la carpeta `frontend/`.

### Backend

- Spring Boot 4.1.1 y Java 25.
- Maven Wrapper.
- MySQL 8 y Spring Data JPA.
- API REST organizada en controladores, servicios, repositorios y DTOs.
- Validaciones con Jakarta Validation.
- Entidades de usuarios, organizaciones y animales.
- Contraseñas protegidas con BCrypt.
- Autenticación con JWT y Spring Security.
- Registro de cuentas de adoptantes y organizaciones.

### Frontend

- Angular 21.2.
- Login implementado con Reactive Forms y cliente HTTP de Angular.
- Formularios de registro de adoptantes y organizaciones implementados con Signal Forms.
- Panel de administración para consultar solicitudes pendientes y organizaciones activas.
- Botones para activar o desactivar perfiles, conectados al endpoint protegido de Spring.
- Login conectado a `https://spring.itechk.us/api/v1/auth/login`.
- Muestra el correo y rol devueltos al iniciar sesión: `ADMIN`, `ORGANIZACION` o `ADOPTANTE`.
- Diseño adaptable a pantallas pequeñas.

En Angular 21.2, Signal Forms es una API experimental. Los formularios de registro incluyen validaciones de campos y se conectan a los endpoints existentes de Spring. Aún no hay paneles distintos por rol, catálogo de mascotas ni carga de imágenes.

## Funcionalidad del backend

### Autenticación y registro

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/v1/auth/register` | Registra una cuenta de adoptante. |
| POST | `/api/v1/auth/register/organizacion` | Registra una cuenta y el perfil de una organización. |
| POST | `/api/v1/auth/login` | Inicia sesión y devuelve el JWT, correo y rol. |
| PATCH | `/api/v1/organizaciones/{id}/activo` | Activa o desactiva un perfil de organización; requiere rol `ADMIN`. |

El registro de una organización desde Angular solicita los datos de la persona responsable y del perfil institucional. Spring crea la cuenta con rol `ORGANIZACION` y deja el perfil inicialmente inactivo (`activo=false`). El formulario informa que el perfil queda pendiente de activación. El administrador puede cambiar el estado con `PATCH /api/v1/organizaciones/{id}/activo`, enviando `{ "activo": true }` para activar o `{ "activo": false }` para desactivar. El endpoint requiere un JWT con autoridad `ADMIN`. La aplicación del estado inactivo en el acceso y en las operaciones de mascotas sigue pendiente.

### Organizaciones

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/v1/organizaciones` | Lista organizaciones. |
| GET | `/api/v1/organizaciones/{id}` | Consulta una organización por ID. |
| POST | `/api/v1/organizaciones` | Crea una organización. |
| PUT | `/api/v1/organizaciones/{id}` | Actualiza una organización. |
| DELETE | `/api/v1/organizaciones/{id}` | Elimina una organización. |

### Animales

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/v1/animales` | Lista animales. |
| GET | `/api/v1/animales/{id}` | Consulta un animal por ID. |
| POST | `/api/v1/animales` | Registra un animal. |
| PUT | `/api/v1/animales/{id}` | Actualiza un animal. |
| DELETE | `/api/v1/animales/{id}` | Elimina un animal. |

Cada animal está asociado a una organización. La asociación y autorización de escritura por organización autenticada aún requieren endurecimiento: actualmente las operaciones de animales no verifican que la organización propietaria corresponda al usuario que presenta el token.

### Seguridad actual

Los endpoints `/api/v1/auth/**` permiten registro e inicio de sesión sin token. El resto de las rutas requiere autenticación JWT. El endpoint `PATCH /api/v1/organizaciones/{id}/activo` exige además el rol `ADMIN`. El panel Angular solo se muestra para una sesión cuyo rol es `ADMIN`. El resto de permisos por rol y el bloqueo operativo de organizaciones inactivas aún requieren implementación.

El CORS del backend permite el origen de desarrollo `http://localhost:4200`. Para usar el frontend desde otro origen habrá que agregarlo explícitamente a la configuración.

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
├── docs/                     # Documentación del proyecto
├── mvnw
└── pom.xml
```

## Requisitos

- Java JDK 25.
- MySQL 8.
- Node.js 24 y npm 11 para el frontend.
- Git.

## Configuración y ejecución

### Backend

Configura la conexión a MySQL en `src/main/resources/application.properties`. Ese archivo contiene credenciales locales y no debe publicarse.

Desde la raíz del repositorio:

```bash
./mvnw spring-boot:run
```

Por defecto, Spring Boot escucha en `http://localhost:8080`.

### Frontend

En otra terminal:

```bash
cd frontend
npm install
npm start -- --host 0.0.0.0
```

Angular inicia el servidor de desarrollo en el puerto 4200. Abre `http://localhost:4200` en el equipo donde corre el navegador. El servidor de desarrollo es para desarrollo y pruebas, no para producción.

## Flujo de autenticación y registro en Angular

### Iniciar sesión

1. La persona ingresa correo y contraseña.
2. El login actual usa Reactive Forms para validar los campos y envía `{ "email": "...", "pass": "..." }` a `POST /api/v1/auth/login`.
3. Si las credenciales son válidas, la pantalla muestra el rol y correo devueltos por Spring.
4. «Cerrar sesión» limpia el estado de sesión en la aplicación.

### Crear una cuenta

Los formularios de registro usan Signal Forms y permiten elegir uno de estos tipos:

- **Adoptante:** nombre, apellidos, correo y contraseña; se envía a `POST /api/v1/auth/register`.
- **Organización:** datos de acceso y perfil (nombre institucional, tipo, correo de contacto, teléfono, dirección, distrito y descripción opcional); se envía a `POST /api/v1/auth/register/organizacion`.

Al completar el registro, Angular vuelve al login y muestra un mensaje de resultado. En el registro de organización aclara que su perfil queda pendiente de activación.

Por ahora el token recibido al iniciar sesión se mantiene solo en el estado de la pantalla; no se persiste al recargar. El panel de administración sí lo adjunta a las consultas y cambios de estado de organizaciones.

## Próximos pasos

- Aplicar el estado inactivo de la organización para impedir el acceso y la publicación de mascotas.
- Implementar la activación administrativa de perfiles de organización y aplicar permisos por rol.
- Vincular las operaciones de mascotas con la organización del usuario autenticado y validar propiedad.
- Implementar catálogo, solicitudes de adopción y vistas según rol.
- Configurar Garage para almacenamiento de imágenes y enlazar la carga desde la aplicación.
- Preparar el despliegue del frontend y backend.

## Proyecto académico

- Curso: Soluciones Web y Aplicaciones Distribuidas.
- Proyecto: Huellitas Conectadas.
- Autor: Alejandro Leon.
