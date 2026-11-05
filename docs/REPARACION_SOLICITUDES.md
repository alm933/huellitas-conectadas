# Reparación de solicitudes de adopción

El registro del servidor del 5 de noviembre de 2026 muestra el error MySQL 1364:
`Field 'motivo' doesn't have a default value`.

La tabla existente exige `motivo`, mientras que la entidad nueva insertaba `mensaje`.
Hibernate `ddl-auto=update` añade columnas, pero no reconoce que un campo fue renombrado.
La entidad ahora conserva el nombre original `motivo`; el contrato JSON mantiene `mensaje`.
Cuando no se escribe un mensaje se guarda «Me interesa adoptar esta mascota.».
No se elimina ninguna columna ni se requiere borrar tablas.

La estructura completa facilitada desde MySQL confirma otros dos campos obligatorios:
`tiene_otras_mascotas bit(1) NOT NULL` y `tipo_vivienda varchar(30) NOT NULL`.
El formulario ahora pide ambas respuestas antes de permitir el envío; Spring las valida
y las guarda explícitamente. No se supone que el adoptante no tiene mascotas ni se inventa
su vivienda. La organización recibe estas respuestas junto con el mensaje.
El cuerpo JSON debe incluir `tieneOtrasMascotas` (booleano) y `tipoVivienda` (texto de hasta 30 caracteres).
Los clientes anteriores que omitan estas respuestas reciben 400, por lo que hay que actualizar
tanto Angular como Spring.

También se corrige el manejo de errores del contenedor: un despacho interno `ERROR`
puede devolver el error original sin que Spring Security lo convierta en un 401.
Las peticiones normales a `/error` y los endpoints de negocio siguen protegidos.
El mensaje de Angular ya no afirma que cualquier 401 signifique que la sesión expiró.

Además se añade el historial del adoptante (`GET /api/v1/solicitudes-adopcion/mias`),
se muestran los motivos de error devueltos por el servidor y se restaura el estado
de solicitud pendiente al entrar de nuevo en el detalle.
La aprobación marca la mascota como adoptada y rechaza las otras solicitudes pendientes.
Las decisiones y creaciones se serializan con bloqueo de la mascota dentro de una transacción.
Eliminar una publicación elimina sus solicitudes antes de eliminar la mascota.

## Actualizar el servidor

Actualizar tanto Spring como Angular desde la rama que contiene esta reparación.
Conservar los archivos locales de configuración y las variables de Garage.
En `/home/ale/proyects/test/demoone`, detener Spring con Ctrl+C y arrancarlo de nuevo:

```bash
set -a
source ~/.config/huellitas/garage.env
set +a
./mvnw spring-boot:run
```

Reiniciar también Angular desde `frontend` y recargar el navegador.
No hace falta ejecutar `DROP`, `TRUNCATE` ni recrear la base de datos.

## Comprobación de la presentación

1. Entrar como adoptante y solicitar una mascota disponible.
2. Ver «Solicitud enviada», volver al catálogo y consultar «Mis solicitudes».
3. Recargar el detalle y verificar que no permite duplicar la solicitud pendiente.
4. Entrar como su organización y comprobar que aparece la solicitud.
5. Aprobarla: la mascota debe figurar como `ADOPTADO` y desaparecer del catálogo disponible.
6. Volver al adoptante y actualizar sus solicitudes: debe aparecer `APROBADA`.

## Alcance de la validación

Las pruebas de Spring usan H2 en modo MySQL y JWT reales generados para cuentas de prueba.
Comprueban el dato guardado en `motivo`, solicitudes sin mensaje, duplicados, historial,
permisos por rol, aprobación, rechazo de las otras solicitudes y eliminación de la publicación.
No sustituyen la comprobación final con la base MySQL existente del servidor.
GitHub Actions ahora ejecuta todas las pruebas del backend.

Los favoritos siguen siendo temporales, como indica el README.
No se han probado cargas reales a Garage ni una sesión completa contra producción.
