# Huellitas Conectadas

Backend inicial de la plataforma **Huellitas Conectadas**, un sistema web que conecta a personas interesadas en adoptar animales con albergues, refugios y veterinarias participantes.

## Versión actual

**V1 - Sprint 1: backend base**

La primera versión incluye:

- Spring Boot 4.1.1.
- Java 25.
- Maven.
- MySQL 8.
- Spring Data JPA.
- API REST.
- Validaciones con Jakarta Validation.
- Entidades `OrganizacionEntity` y `AnimalEntity`.
- Relaciones entre organizaciones y animales.
- DTOs de entrada y salida.
- Servicios y repositorios.
- Manejo básico de excepciones.

## Funcionalidad actual

Las organizaciones pueden representar albergues, refugios o veterinarias.

Cada organización puede publicar varios animales. Cada animal pertenece a una organización mediante una relación muchos-a-uno.

### Endpoints de organizaciones

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/v1/organizaciones` | Listar organizaciones |
| GET | `/api/v1/organizaciones/{id}` | Consultar una organización |
| POST | `/api/v1/organizaciones` | Registrar una organización |
| PUT | `/api/v1/organizaciones/{id}` | Actualizar una organización |
| DELETE | `/api/v1/organizaciones/{id}` | Eliminar una organización |

### Endpoints de animales

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/v1/animales` | Listar animales |
| GET | `/api/v1/animales/{id}` | Consultar un animal |
| POST | `/api/v1/animales` | Registrar un animal |
| PUT | `/api/v1/animales/{id}` | Actualizar un animal |
| DELETE | `/api/v1/animales/{id}` | Eliminar un animal |

## Modelo inicial

### OrganizacionEntity

- Identificador.
- Nombre.
- Tipo de organización.
- Correo.
- Teléfono.
- Dirección.
- Distrito.
- Descripción.
- Fotografía.
- Estado activo.
- Fecha de registro.

### AnimalEntity

- Identificador.
- Nombre.
- Especie.
- Raza.
- Edad aproximada en meses.
- Sexo.
- Tamaño.
- Historia o descripción.
- Fotografía.
- Estado de adopción.
- Fecha de publicación.
- Organización responsable.

## Estados del animal

- `DISPONIBLE`
- `EN_PROCESO`
- `ADOPTADO`
- `NO_DISPONIBLE`

## Arquitectura

El proyecto utiliza una arquitectura por capas:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
MySQL
```

También se utilizan DTOs para evitar exponer directamente las entidades JPA.

## Requisitos

- Java JDK 25.
- Maven Wrapper incluido en el proyecto.
- MySQL 8.
- Git.

## Ejecución

Configurar las credenciales de MySQL en un archivo local:

`src/main/resources/application.properties`

No publicar dicho archivo porque contiene credenciales de la base de datos.

Para iniciar el proyecto:

```bash
./mvnw spring-boot:run
```

La aplicación se ejecuta por defecto en:

```text
http://localhost:8080
```

## Próximas versiones

- Agregar autenticación de usuarios.
- Implementar Spring Security.
- Utilizar tokens JWT.
- Crear roles para administradores, organizaciones y adoptantes.
- Agregar solicitudes de adopción.
- Incorporar filtros para el feed.
- Desarrollar el frontend.
- Desplegar la aplicación desde una mini PC con acceso mediante túnel seguro.

## Proyecto académico

Curso: Soluciones Web y Aplicaciones Distribuidas

Proyecto: Huellitas Conectadas

Autor: Alejandro Leon



## 2. Actualizar el `README.md`

Agrega una sección como esta:

```markdown
## Estado actual

La versión actual incluye:

- CRUD de organizaciones.
- CRUD de animales.
- Registro de usuarios.
- Login de usuarios.
- BCrypt para proteger contraseñas.
- Generación de tokens JWT.
- Spring Security configurado.
- Persistencia de tokens en MySQL.

### Endpoints de autenticación

```http
POST /api/v1/auth/register
POST /api/v1/auth/login


## 3.