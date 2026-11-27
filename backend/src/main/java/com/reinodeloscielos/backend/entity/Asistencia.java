
package com.reinodeloscielos.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

@Entity
@Table(
    name = "asistencias",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_asistencia_alumno_curso_fecha",
            columnNames = {"alumno_id", "curso_id", "fecha"}
        )
    }
)
public class Asistencia {

    public enum Estado {
        ASISTIO,
        FALTA,
        TARDANZA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El alumno es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;

    @NotNull(message = "El curso es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @NotNull(message = "La fecha de asistencia es obligatoria")
    @PastOrPresent(message = "La fecha de asistencia no puede ser futura")
    @Column(nullable = false)
    private LocalDate fecha;

    @NotNull(message = "El estado de asistencia es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Estado estado;

    public Asistencia() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }
}
