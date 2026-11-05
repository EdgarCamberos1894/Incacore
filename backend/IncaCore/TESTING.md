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

Estas pruebas no equivalen a una verificación transaccional con PostgreSQL:
los movimientos con múltiples detalles todavía requieren pruebas de integración
para comprobar atomicidad y consistencia ante errores intermedios.
