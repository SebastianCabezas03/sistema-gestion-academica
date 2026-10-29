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
$env:ADMIN_PASSWORD = Read-Host "Contrasena del administrador inicial"
./mvnw.cmd spring-boot:run
```

La conexion predeterminada es localhost:3306/gestion_academica, usuario root,
sin contrasena. Si la instalacion es diferente, definir DB_URL, DB_USERNAME y
DB_PASSWORD antes del arranque. DB_URL debe usar jdbc:mysql://.
Las variables del proceso tienen prioridad sobre los valores predeterminados.
No publicar contrasenas. Spring Boot no carga archivos .env automaticamente.

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

Los scripts corresponden a las entidades actuales: usuarios, alumnos, docente,
curso y matriculas. Los diagramas del informe pueden incluir funciones propuestas
que aun no estan implementadas. Los datos de prueba no se incluyen en el repositorio.

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

## Autorizacion pendiente

El login verifica credenciales, pero la API conserva permitAll del principal.
El guard solo protege navegacion. Completar autorizacion del backend antes de
un despliegue publico; esta integracion no implementa JWT ni sesiones autenticadas.

## Resultado de la comprobacion de MySQL

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
