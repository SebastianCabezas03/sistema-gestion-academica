# Matriculas con MySQL de XAMPP

La rama incorpora el main del equipo y mantiene el modulo de matriculas.
El backend conserva el conector JDBC MySQL del principal. El volcado original
del equipo identifica MariaDB 10.4.32; XAMPP presenta ese servicio como MySQL.

## Ejecutar en Windows

Requisitos: XAMPP, JDK 21 y Node.js compatible con Angular 20.
Iniciar MySQL en XAMPP y crear gestion_academica con utf8mb4.
Apache solo es necesario si se administra mediante phpMyAdmin.

Desde PowerShell, en backend:

```powershell
$env:ADMIN_PASSWORD = Read-Host "Contraseña del administrador inicial" -MaskInput
$env:APP_JWT_SECRET = Read-Host "Clave JWT en Base64" -MaskInput
./mvnw.cmd spring-boot:run
```

La conexion predeterminada es localhost:3306/gestion_academica, usuario root,
sin contrasena. Si la instalacion es diferente, definir DB_URL, DB_USERNAME y
DB_PASSWORD antes del arranque. DB_URL debe usar jdbc:mysql://.
Las variables del proceso tienen prioridad sobre los valores predeterminados.
Spring Boot no carga archivos .env automáticamente. APP_JWT_SECRET es obligatoria
y debe contener al menos 32 bytes aleatorios codificados en Base64. En PowerShell 7
puedes generar una clave con el siguiente comando y guardarla fuera del repositorio.

```powershell
[Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
```

ADMIN_PASSWORD crea admin si no existe; no cambia la clave de una cuenta existente.
Si no existe admin y falta la variable, se informa y no se crea la cuenta.

Desde frontend: npm ci y npm start. Abrir http://localhost:4200/login.
Registrar primero alumnos y cursos y luego acceder a /matriculas.

## Base de datos

Hibernate crea o actualiza las tablas con DB_DDL_AUTO=update por defecto.
Como alternativa, importar database/gestion_academica.sql en una base vacia.
Para un proyecto existente con alumnos y curso, database/matriculas.sql agrega
la tabla de inscripciones. Ejecutar los scripts en la base seleccionada.
Usar InnoDB y referencias BIGINT con signo para las claves foraneas.
CREATE TABLE IF NOT EXISTS no convierte tablas ni migra esquemas existentes.
Respaldar una base con informacion antes de cualquier cambio de estructura.

El script completo incluye cuentas vinculadas a alumnos y docentes, cursos,
matrículas con nota y asistencias. Para actualizar una base existente, respáldala
y verifica el arranque con DB_DDL_AUTO=update sobre una copia antes de aplicarlo
a la base que usas.

## Comportamiento

- Inscripcion en varios cursos en una operacion atomica.
- Combinacion alumno/curso unica, incluso si fue cancelada.
- Edicion, cancelacion y reactivacion conservando el mismo identificador.
- Inicio carga los cuatro totales por separado e incluye matriculas canceladas.
- Alumnos y cursos con matriculas conservan sus referencias; no se borran en cascada.

## Verificacion

Frontend: npm run build y npm run test:ci (Chrome instalado).
Backend: ./mvnw.cmd test o ./mvnw.cmd package, con base de pruebas y variables
de conexion configuradas. Estas pruebas no deben ejecutarse sobre datos del equipo.

## Acceso y calificaciones

El inicio de sesión devuelve un JWT. En **Calificaciones**, el docente elige
un curso asignado y registra o corrige la nota final de cada matrícula activa.
La escala va de 0 a 20 y admite hasta dos decimales. En **Mis notas**, el alumno
ve sus calificaciones y las pendientes. Una matrícula cancelada conserva su nota,
que vuelve a mostrarse al reactivarla. Si la matrícula ya tiene nota, conserva
también su alumno y su curso.

Para verificar el flujo, vincula las cuentas de docente y alumno, asigna el curso
al docente y matricula al alumno. Guarda una nota como docente y comprueba el
resultado como alumno. Verifica que otro docente carezca de acceso a ese curso
y que se rechacen notas fuera de rango.

## Publicación

Angular usa rutas relativas bajo `/api`. En desarrollo, `npm start`
reenvía estas solicitudes al backend local con `proxy.conf.json`. En producción,
sirve Angular y `/api` bajo el mismo origen. Configura el servidor para reenviar
`/api` al backend y servir `index.html` en las rutas de Angular.

## Comprobación histórica de MySQL

Validado el 28 de octubre de 2026 con MySQL Connector/J y MariaDB 10.4.32
portatil, la version indicada en el SQL original del equipo. No se instalo XAMPP
en este entorno ni se probo por separado MySQL Server 8.

- Backend empaquetado: 8 pruebas correctas, incluido arranque con base real.
- Scripts importados en una base vacia y validados por Hibernate con ddl-auto=validate.
- 22 comprobaciones HTTP correctas: login valido e invalido, registros, duplicados,
  lote atomico, fecha futura, seleccion vacia, cancelacion idempotente y reactivacion.
- Datos conservados tras reiniciar el backend.
- Reaplicacion del script sin borrar los registros de prueba.
- Frontend compilado en produccion con advertencias existentes de tamano de estilos.

La comprobacion no equivale a una auditoria de seguridad ni valida una base del
equipo con cambios de esquema desconocidos.
