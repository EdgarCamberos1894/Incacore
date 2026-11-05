# Pruebas automatizadas del backend

Las pruebas JUnit 5 + Mockito usan dependencias simuladas: no requieren la
base PostgreSQL, autenticación real, envíos de correo ni proveedores externos.

- `VesselServiceImplTest`: creación de embarcaciones y rechazo de nombres duplicados.
- `RegistrationAccessPolicyTest`: registro público deshabilitado/habilitado,
  privilegios de administrador y rechazo de autoridades no autenticadas.
- `InventoryMovementServiceImpTest`: reversión de entradas/salidas de stock,
  rechazo de movimientos inexistentes y detención de la eliminación si falla
  la actualización de existencias.

Desde `backend/IncaCore/`:

```bash
./mvnw -B -ntp test
```

El workflow `backend-unit-tests.yml` corre en cada PR y push a
`feature/portfolio-demo`, la rama usada por el despliegue público de INCACORE.

## Integración transaccional con PostgreSQL real

`InventoryMovementTransactionIT` utiliza Testcontainers + PostgreSQL 16 y
verifica diez escenarios: creación y eliminación exitosas, rollback en
creación y eliminación si falla el stock de un segundo ítem después de haber
actualizado el primero, y dos operaciones concurrentes sobre un mismo stock
(incluyendo movimientos de varios ítems en orden inverso). También cubre el
envío de alertas de stock únicamente tras un commit, su omisión ante rollback
y que un error del proveedor no revierta el inventario confirmado. Los datos son
temporales; Hibernate genera el esquema `incacore` sin aplicar las migraciones
productivas.

Se requiere Docker para ejecutar las pruebas de integración localmente:

```bash
./mvnw -B -ntp '-Dtest=*IT' test
```

El workflow `postgres-integration-tests.yml` las ejecuta en CI con Docker
en cada PR y push relevante a `feature/portfolio-demo`. El comando normal
`./mvnw test` sigue ejecutando únicamente las pruebas unitarias rápidas.

Importante: estas pruebas desactivan la transacción que `@DataJpaTest` abre
por defecto. Así verifican que **el servicio** define los límites de la
transacción y que las escrituras ya confirmadas se revierten ante un fallo.
Los tests concurrentes comprueban que el segundo escritor espere al primero
y que ambas operaciones acumulen sus cambios. Las filas de stock se bloquean
con `PESSIMISTIC_WRITE` en una sola consulta sin joins (evitando el bloqueo
posterior a una lectura obsoleta) dentro de transacciones del servicio, y los
movimientos de varios ítems adquieren bloqueos por ID ascendente para reducir
interbloqueos. Las alertas se publican como eventos inmutables y se entregan
mediante `@TransactionalEventListener(AFTER_COMMIT)`, sin bloquear el commit
ni alterar su resultado si el proveedor falla. La entrega es de mejor esfuerzo:
no se garantiza reintento ni persistencia de la alerta si el proceso cae justo
después del commit. Eso requeriría un outbox transaccional y un worker.
Tampoco se validan aquí otros flujos que modifiquen existencias fuera de estos
servicios.
