-- MySQL/MariaDB de XAMPP. Seleccionar primero la base del proyecto.
-- Requiere alumnos(id) y curso(id), BIGINT con signo y motor InnoDB.
-- No modifica tablas existentes ni elimina datos.
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
