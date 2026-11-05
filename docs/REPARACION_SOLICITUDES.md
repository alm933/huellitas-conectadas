# Reparación de solicitudes de adopción

El registro del servidor del 5 de noviembre de 2026 muestra el error MySQL 1364:
`Field 'motivo' doesn't have a default value`.

La tabla existente exige `motivo`, mientras que la entidad nueva insertaba `mensaje`.
Hibernate `ddl-auto=update` añade columnas, pero no reconoce que un campo fue renombrado.
La entidad ahora conserva el nombre original `motivo`; el contrato JSON mantiene `mensaje`.
Cuando no se escribe un mensaje se guarda «Me interesa adoptar esta mascota.».
No se elimina ninguna columna ni se requiere borrar tablas.

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
