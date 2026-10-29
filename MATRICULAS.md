# Módulo de matrículas

Una matrícula vincula un alumno con un curso. Se puede inscribir al mismo alumno
en varios cursos en una operación: si alguno no existe o ya tiene matrícula,
se rechaza todo el lote sin guardar inscripciones parciales.

## Puesta en marcha

La base de datos es MySQL/MariaDB de XAMPP. Consulta `INTEGRACION.md` para configurar
las variables de entorno, crear el administrador e iniciar ambos servicios.
Tras iniciar sesión, abre `http://localhost:4200/matriculas`.

## Operaciones

| Método | Ruta | Función |
| --- | --- | --- |
| GET | `/api/matriculas` | Listar con nombres de alumno y curso |
| POST | `/api/matriculas` | Registrar uno o varios cursos de forma atómica |
| PUT | `/api/matriculas/{id}` | Editar alumno, curso, fecha y estado |
| PATCH | `/api/matriculas/{id}/cancelar` | Cancelar conservando el historial |

Ejemplo de registro:

```json
{"alumnoId": 1, "cursosIds": [1, 2], "fecha": "2026-10-26"}
```

La fecha es obligatoria y no puede ser futura. Para editar se envían `alumnoId`,
`cursoId`, `fecha` y `estado` (`ACTIVA` o `CANCELADA`). Una matrícula cancelada
también ocupa la combinación alumno/curso: se reactiva con Editar, sin duplicarla.
No se implementan periodos académicos ni pagos en este alcance.

La cancelación es idempotente. Las claves foráneas impiden borrar alumnos o cursos
con matrículas; cancelar no elimina esa relación ni su historial.

## Comprobaciones

- Registrar dos cursos y comprobar que aparecen dos filas y aumenta el contador.
- Repetir alumno/curso: debe responder 409 y no crear filas.
- Enviar un lote con un curso inexistente: 404 sin guardar ninguna matrícula.
- Enviar fecha futura, alumno nulo o selección vacía: 400.
- Editar una inscripción, cancelarla y reactivarla: conserva su ID.
- Registrar simultáneamente la misma combinación: solo debe persistir una fila.
- Buscar por nombre y filtrar activas/canceladas; comprobar errores y lista vacía.

Pruebas del servicio: desde `backend`, `./mvnw.cmd -Dtest=MatriculaServiceTests test`.
Compilación de la interfaz: desde `frontend`, `npm run build`.

Pruebas aisladas de la interfaz: `npm run test:matriculas` (requiere Chrome).
También se puede ejecutar toda la suite con `npm run test:ci`.
Las pruebas Java requieren JDK 21. La comprobación de extremo a extremo requiere
MySQL/MariaDB iniciado y datos de alumnos/cursos.
