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
verifica seis escenarios: creación y eliminación exitosas, rollback en
creación y eliminación si falla el stock de un segundo ítem después de haber
actualizado el primero, y dos operaciones concurrentes sobre un mismo stock
(incluyendo movimientos de varios ítems en orden inverso). Los datos son
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
con `PESSIMISTIC_WRITE` dentro de transacciones del servicio, y los
movimientos de varios ítems adquieren bloqueos por ID ascendente para reducir
interbloqueos. No se valida todavía la coordinación del envío de alertas de
correo con el commit ni otros flujos que puedan modificar existencias fuera de
estos servicios.
