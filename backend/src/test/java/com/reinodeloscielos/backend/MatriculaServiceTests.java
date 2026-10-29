package com.reinodeloscielos.backend;

import com.reinodeloscielos.backend.dto.*;
import com.reinodeloscielos.backend.entity.*;
import com.reinodeloscielos.backend.repository.*;
import com.reinodeloscielos.backend.service.MatriculaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MatriculaServiceTests {
    private final MatriculaRepository matriculas = mock(MatriculaRepository.class);
    private final AlumnoRepository alumnos = mock(AlumnoRepository.class);
    private final CursoRepository cursos = mock(CursoRepository.class);
    private final MatriculaService service = new MatriculaService(matriculas, alumnos, cursos);
    private Alumno alumno;
    private Curso curso;

    @BeforeEach void preparar() {
        alumno = new Alumno(); alumno.setId(1L); alumno.setNombres("Ana"); alumno.setApellidos("Pérez");
        curso = new Curso(); curso.setId(2L); curso.setNombreCurso("Matemática");
        when(alumnos.findById(1L)).thenReturn(Optional.of(alumno));
        when(cursos.findById(2L)).thenReturn(Optional.of(curso));
    }

    @Test void registraVariosCursos() {
        Curso otro = new Curso(); otro.setId(3L); otro.setNombreCurso("Historia");
        when(cursos.findById(3L)).thenReturn(Optional.of(otro));
        when(matriculas.saveAllAndFlush(anyList())).thenAnswer(i -> i.getArgument(0));
        var resultado = service.registrar(new MatriculaRequestDto(1L, List.of(2L, 3L), LocalDate.now()));
        assertEquals(2, resultado.size());
        assertEquals("Ana Pérez", resultado.get(0).nombreAlumno());
        assertTrue(resultado.stream().allMatch(m -> m.estado() == Matricula.Estado.ACTIVA));
    }

    @Test void cursoInexistenteNoGuardaParteDelLote() {
        when(cursos.findById(3L)).thenReturn(Optional.empty());
        var ex = assertThrows(ResponseStatusException.class,
                () -> service.registrar(new MatriculaRequestDto(1L, List.of(2L, 3L), LocalDate.now())));
        assertEquals(404, ex.getStatusCode().value());
        verify(matriculas, never()).saveAllAndFlush(anyList());
    }

    @Test void duplicadoInclusoCanceladoSeRechaza() {
        when(matriculas.existsByAlumnoIdAndCursoId(1L, 2L)).thenReturn(true);
        var ex = assertThrows(ResponseStatusException.class,
                () -> service.registrar(new MatriculaRequestDto(1L, List.of(2L), LocalDate.now())));
        assertEquals(409, ex.getStatusCode().value());
        verify(matriculas, never()).saveAllAndFlush(anyList());
    }

    @Test void listaConCursosRepetidosSeRechaza() {
        var ex = assertThrows(ResponseStatusException.class,
                () -> service.registrar(new MatriculaRequestDto(1L, List.of(2L, 2L), LocalDate.now())));
        assertEquals(400, ex.getStatusCode().value());
        verify(matriculas, never()).saveAllAndFlush(anyList());
    }

    @Test void cancelarConservaRelacionYEsIdempotente() {
        Matricula m = existente();
        assertEquals(Matricula.Estado.CANCELADA, service.cancelar(4L).estado());
        assertEquals(Matricula.Estado.CANCELADA, service.cancelar(4L).estado());
        assertSame(alumno, m.getAlumno()); assertSame(curso, m.getCurso());
        verify(matriculas, never()).delete(any());
    }

    @Test void permiteReactivarLaMismaMatricula() {
        Matricula m = existente(); m.setEstado(Matricula.Estado.CANCELADA);
        var dto = new MatriculaUpdateDto(1L, 2L, LocalDate.now(), Matricula.Estado.ACTIVA);
        assertEquals(Matricula.Estado.ACTIVA, service.actualizar(4L, dto).estado());
    }

    @Test void editarNoPuedeDuplicarOtraMatricula() {
        existente();
        when(matriculas.existsByAlumnoIdAndCursoIdAndIdNot(1L, 2L, 4L)).thenReturn(true);
        var ex = assertThrows(ResponseStatusException.class, () -> service.actualizar(4L,
                new MatriculaUpdateDto(1L, 2L, LocalDate.now(), Matricula.Estado.ACTIVA)));
        assertEquals(409, ex.getStatusCode().value());
        verify(matriculas, never()).saveAndFlush(any());
    }

    private Matricula existente() {
        Matricula m = new Matricula(); m.setAlumno(alumno); m.setCurso(curso); m.setFecha(LocalDate.now());
        when(matriculas.findById(4L)).thenReturn(Optional.of(m));
        when(matriculas.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        return m;
    }
}
