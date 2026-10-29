-- Esquema del codigo implementado, para MySQL/MariaDB de XAMPP.
-- Seleccionar primero gestion_academica en phpMyAdmin o la consola.
-- No contiene cuentas, contrasenas ni datos personales.
-- IF NOT EXISTS no migra tablas ya creadas. Revisarlas antes de importar.
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS alumnos (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    correo VARCHAR(150) UNIQUE,
    telefono VARCHAR(20)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS docente (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombres VARCHAR(255), apellidos VARCHAR(255), correo VARCHAR(255),
    telefono VARCHAR(255), especialidad VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS curso (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre_curso VARCHAR(255), docente_id BIGINT,
    CONSTRAINT fk_curso_docente FOREIGN KEY (docente_id) REFERENCES docente(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS matriculas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    alumno_id BIGINT NOT NULL,
    curso_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    estado VARCHAR(10) NOT NULL DEFAULT 'ACTIVA',
    PRIMARY KEY (id),
    CONSTRAINT uk_matricula_alumno_curso UNIQUE (alumno_id, curso_id),
    CONSTRAINT fk_matricula_alumno FOREIGN KEY (alumno_id) REFERENCES alumnos(id),
    CONSTRAINT fk_matricula_curso FOREIGN KEY (curso_id) REFERENCES curso(id),
    CONSTRAINT ck_matricula_estado CHECK (estado IN ('ACTIVA', 'CANCELADA'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
