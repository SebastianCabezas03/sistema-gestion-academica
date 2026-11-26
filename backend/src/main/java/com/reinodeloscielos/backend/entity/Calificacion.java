
package com.reinodeloscielos.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

@Entity
@Table(
    name = "calificaciones",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_calificacion_alumno_evaluacion",
            columnNames = {"alumno_id", "evaluacion_id"}
        )
    }
)
public class Calificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluacion_id", nullable = false)
    private Evaluacion evaluacion;

    @DecimalMin(value = "0.00", message = "La nota mínima es 0")
    @DecimalMax(value = "20.00", message = "La nota máxima es 20")
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal nota;

    public Calificacion() {
    }

    public Long getId() {
        return id;
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    public Evaluacion getEvaluacion() {
        return evaluacion;
    }

    public void setEvaluacion(Evaluacion evaluacion) {
        this.evaluacion = evaluacion;
    }

    public BigDecimal getNota() {
        return nota;
    }

    public void setNota(BigDecimal nota) {
        this.nota = nota;
    }
}
