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
- Formularios reactivos y cliente HTTP de Angular.
- Pantalla de inicio de sesión conectada a `https://spring.itechk.us/api/v1/auth/login`.
- Al iniciar sesión correctamente, muestra el correo y el rol recibido: `ADMIN`, `ORGANIZACION` o `ADOPTANTE`.
- Diseño adaptable a pantallas pequeñas.

El frontend actual es una primera pantalla funcional de login. Todavía no incluye formularios de registro, paneles diferentes por rol, catálogo de mascotas ni carga de imágenes.

## Funcionalidad del backend

### Autenticación y registro

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/v1/auth/register` | Registra una cuenta de adoptante. |
| POST | `/api/v1/auth/register/organizacion` | Registra una cuenta y el perfil de una organización. |
| POST | `/api/v1/auth/login` | Inicia sesión y devuelve el JWT, correo y rol. |

El registro de una organización crea la cuenta con rol `ORGANIZACION` y un perfil inicialmente inactivo (`activo=false`). El endpoint de activación administrativa y la aplicación efectiva de ese estado en los permisos están pendientes.

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

Los endpoints `/api/v1/auth/**` permiten registro e inicio de sesión sin token. El resto de las rutas requiere autenticación JWT. La configuración aún no aplica permisos distintos por rol; por ello, el rol que muestra Angular sirve actualmente para confirmar la respuesta del login, no para proteger vistas o acciones.

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

## Flujo de inicio de sesión Angular

1. La persona ingresa correo y contraseña.
2. Angular valida los campos y envía `{ "email": "...", "pass": "..." }` a `POST /api/v1/auth/login`.
3. Si las credenciales son válidas, muestra el rol y el correo devueltos por Spring.
4. El botón «Cerrar sesión» limpia el estado de sesión en la aplicación.

Por ahora el token se mantiene solo en el estado de la pantalla; no se persiste al recargar ni se adjunta automáticamente a solicitudes posteriores.

## Próximos pasos

- Crear en Angular el formulario de registro para adoptantes y organizaciones.
- Implementar la activación administrativa de perfiles de organización y aplicar permisos por rol.
- Vincular las operaciones de mascotas con la organización del usuario autenticado y validar propiedad.
- Implementar catálogo, solicitudes de adopción y vistas según rol.
- Configurar Garage para almacenamiento de imágenes y enlazar la carga desde la aplicación.
- Preparar el despliegue del frontend y backend.

## Proyecto académico

- Curso: Soluciones Web y Aplicaciones Distribuidas.
- Proyecto: Huellitas Conectadas.
- Autor: Alejandro Leon.
