package com.reinodeloscielos.backend.controller;

import com.reinodeloscielos.backend.entity.Matricula;
import com.reinodeloscielos.backend.entity.Usuario;
import com.reinodeloscielos.backend.repository.CursoRepository;
import com.reinodeloscielos.backend.repository.MatriculaRepository;
import com.reinodeloscielos.backend.repository.UsuarioRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/calificaciones")
public class CalificacionController {
    private final MatriculaRepository matriculas;
    private final CursoRepository cursos;
    private final UsuarioRepository usuarios;

    public CalificacionController(MatriculaRepository matriculas, CursoRepository cursos,
                                  UsuarioRepository usuarios) {
        this.matriculas = matriculas;
        this.cursos = cursos;
        this.usuarios = usuarios;
    }

    @GetMapping("/docente/cursos/{cursoId}")
    @Transactional(readOnly = true)
    public List<NotaDocente> listar(@PathVariable Long cursoId, Authentication sesion) {
        verificarCurso(cursoId, sesion);
        return matriculas.findByCursoIdAndEstadoOrderByAlumnoApellidosAsc(cursoId, Matricula.Estado.ACTIVA)
                .stream().map(this::notaDocente).toList();
    }

    @PutMapping("/docente/cursos/{cursoId}/matriculas/{matriculaId}")
    @Transactional
    public NotaDocente guardar(@PathVariable Long cursoId, @PathVariable Long matriculaId,
                               @Valid @RequestBody NotaRequest solicitud, Authentication sesion) {
        verificarCurso(cursoId, sesion);
        Matricula matricula = matriculas.findById(matriculaId)
                .filter(m -> cursoId.equals(m.getCurso().getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (matricula.getEstado() != Matricula.Estado.ACTIVA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La matrícula está cancelada.");
        }
        matricula.setNota(solicitud.nota());
        return notaDocente(matricula);
    }

    @GetMapping("/mis-notas")
    @Transactional(readOnly = true)
    public List<NotaAlumno> misNotas(Authentication sesion) {
        Usuario usuario = usuario(sesion);
        if (usuario.getAlumno() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return matriculas.findByAlumnoIdAndEstadoOrderByIdDesc(usuario.getAlumno().getId(), Matricula.Estado.ACTIVA)
                .stream().map(m -> new NotaAlumno(m.getId(), m.getCurso().getNombreCurso(), m.getNota()))
                .toList();
    }

    private void verificarCurso(Long cursoId, Authentication sesion) {
        var docente = usuario(sesion).getDocente();
        var curso = cursos.findById(cursoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (docente == null || curso.getDocente() == null ||
                !docente.getId().equals(curso.getDocente().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private Usuario usuario(Authentication sesion) {
        return usuarios.findByUsername(sesion.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
    }

    private NotaDocente notaDocente(Matricula matricula) {
        var alumno = matricula.getAlumno();
        return new NotaDocente(matricula.getId(), alumno.getNombres() + " " + alumno.getApellidos(),
                matricula.getNota());
    }

    public record NotaRequest(@NotNull @DecimalMin("0") @DecimalMax("20")
                              @Digits(integer = 2, fraction = 2) BigDecimal nota) {}

    public record NotaDocente(Long matriculaId, String alumno, BigDecimal nota) {}

    public record NotaAlumno(Long matriculaId, String curso, BigDecimal nota) {}
}
