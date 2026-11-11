# Inventario de Activos de TI — Colegio Ayacucho

Sistema monolítico (CMDB) para la Gestión de Servicios de TI basada en ITIL de un
colegio privado de Ayacucho, Perú.

- **Stack:** Java 17 · Spring Boot 3.5.16 · Spring MVC · Spring Data JPA (Hibernate)
  · Spring Security · Bean Validation · Thymeleaf + extras Spring Security 6
  · PostgreSQL 16 · Bootstrap 5 y Bootstrap Icons por CDN · Lombok · Maven.
- **Un solo proyecto, una sola aplicación, un solo despliegue.**

---

## 1. Requisitos

| Herramienta | Versión |
|---|---|
| JDK | 17 o superior (probado con 21, compilando a 17) |
| PostgreSQL | 12 o superior (probado con 16) |
| Maven | No hace falta instalarlo: se usa el wrapper `mvnw` |

---

## 2. Crear la base de datos en pgAdmin 4

1. Abrir pgAdmin 4 y conectarse al servidor local (`localhost:5432`).
2. Clic derecho en **Databases → Create → Database…**
   - **Database:** `cole`
   - **Owner:** `postgres`
3. Seleccionar la base `cole` y abrir **Tools → Query Tool**.
4. Abrir el archivo `database/schema.sql` y ejecutarlo con **F5**.

> También puede dejar que Hibernate cree el esquema solo
> (`spring.jpa.hibernate.ddl-auto=update`), pero el script deja el esquema
> versionado y con sus restricciones CHECK e índices.

---

## 3. Variables de entorno

La aplicación lee la conexión de variables de entorno, con valores por defecto
**solo para desarrollo**:

| Variable | Valor por defecto | Descripción |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/cole` | URL JDBC |
| `DB_USER` | `postgres` | Usuario de PostgreSQL |
| `DB_PASSWORD` | `root` | Contraseña de PostgreSQL |
| `SERVER_PORT` | `8080` | Puerto HTTP |
| `COOKIE_SECURE` | `false` | Poner `true` cuando se use HTTPS |

**Importante:** si su usuario `postgres` tiene otra contraseña, debe definir
`DB_PASSWORD` con la suya; de lo contrario la aplicación no podrá conectarse.

### Windows (PowerShell, sesión actual)

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/cole"
$env:DB_USER="postgres"
$env:DB_PASSWORD="root"
```

En IntelliJ IDEA: **Run → Edit Configurations → Environment variables**.

---

## 4. Compilar y ejecutar

```powershell
# Pruebas (usan H2 en memoria, no tocan PostgreSQL)
.\mvnw.cmd test

# Ejecutar en desarrollo
.\mvnw.cmd spring-boot:run

# O generar el JAR y ejecutarlo
.\mvnw.cmd -DskipTests package
java -jar target\inventario-ti.jar
```

Abrir: **http://localhost:8080**

---

## 5. Usuarios iniciales

Se crean solo si la tabla `usuarios` está vacía:

| Usuario | Contraseña temporal | Rol |
|---|---|---|
| `admin` | `Admin123*` | ADMIN |
| `tecnico1` | `Tecnico123*` | TECNICO |

Ambos son obligados a cambiar la contraseña en el primer ingreso.

---

## 6. Rutas principales

| Ruta | Descripción | Rol |
|---|---|---|
| `/login` | Inicio de sesión | Público |
| `/dashboard` | Panel con indicadores | Autenticado |
| `/activos` | Listado, búsqueda, filtros y paginación | Autenticado |
| `/activos/nuevo`, `/activos/{id}/editar` | Alta y edición | Autenticado |
| `/activos/{id}` | Detalle + pestaña Historial | Autenticado |
| `/activos/exportar` | CSV del filtro actual (UTF-8 con BOM) | Autenticado |
| `POST /activos/{id}/eliminar` | Baja de activo | Solo ADMIN |
| `/tipos`, `/ubicaciones`, `/usuarios` | Catálogos y usuarios | Solo ADMIN |
| `/perfil/cambiar-password` | Cambio de contraseña | Autenticado |

---

## 7. Reglas de negocio implementadas

- Código de activo único con formato `CI-001` (validación en formulario, servicio
  y restricción CHECK en la base de datos).
- La fecha de fin de garantía no puede ser anterior a la de compra.
- Semáforo de garantía: rojo (vencida), amarillo (vence en ≤ 60 días),
  verde (vigente), gris (sin registro).
- Paginación de 10 registros y filtros combinables por texto, tipo, estado y
  ubicación. La exportación CSV respeta exactamente los filtros aplicados.
- Auditoría automática de creación, edición y eliminación en `historial_activo`.
  Al eliminar un activo su historial se conserva (`activo_id` queda en `NULL`).
- No se puede borrar un tipo o una ubicación con activos asociados.
- No se puede eliminar el propio usuario ni dejar el sistema sin un ADMIN activo.
- Protección CSRF en todos los formularios, contraseñas BCrypt y sesión con
  cookie `HttpOnly` + `SameSite=Lax`.

---

## 8. Pruebas automatizadas

```
.\mvnw.cmd test
```

- `GarantiaUtilTest` (6): regla del semáforo de garantías y límites de 60 días.
- `ActivoServiceTest` (4): código duplicado, alta con historial, fechas
  incoherentes y baja con auditoría.
- `CsvServiceTest` (3): BOM UTF-8, cabecera y entrecomillado de campos.
- `WebSmokeTest` (12): renderizado real de login, panel, listado, detalle,
  formulario, catálogos, 403, CSRF y descarga CSV.
- `ColegioAyacuchoApplicationTests` (1): carga del contexto.

Resultado esperado: **26 pruebas, 0 fallos**.
