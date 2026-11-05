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
verifica cuatro escenarios: creación y eliminación exitosas, y rollback en
creación y eliminación si falla el stock de un segundo ítem después de haber
actualizado el primero. Los datos son temporales, el esquema `incacore` se
genera mediante Hibernate y no se aplican migraciones productivas.

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
No validan todavía concurrencia sobre la misma existencia (lost updates) ni
el envío de alertas de correo coordinado con el commit de la transacción.
