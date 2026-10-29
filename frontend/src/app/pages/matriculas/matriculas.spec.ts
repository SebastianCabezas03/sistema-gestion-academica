import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { Matriculas } from './matriculas';
import { Matricula, MatriculaService } from '../../services/matricula-service';
import { AlumnoService } from '../../services/alumno';
import { CursoService } from '../../services/curso-service';
import { HttpErrorResponse } from '@angular/common/http';

describe('Matrículas', () => {
  let component: Matriculas;
  let service: jasmine.SpyObj<MatriculaService>;
  const matricula: Matricula = { id: 1, alumnoId: 2, nombreAlumno: 'Ana Pérez', cursoId: 3,
    nombreCurso: 'Historia', fecha: '2026-01-01', estado: 'ACTIVA' };
  beforeEach(() => {
    service = jasmine.createSpyObj('MatriculaService', ['listar', 'registrar', 'actualizar', 'cancelar']);
    service.listar.and.returnValue(of([matricula]));
    TestBed.configureTestingModule({ providers: [
      { provide: MatriculaService, useValue: service },
      { provide: AlumnoService, useValue: { listar: () => of([]) } },
      { provide: CursoService, useValue: { listar: () => of([]) } }
    ] });
    component = TestBed.runInInjectionContext(() => new Matriculas());
  });
  it('registra todos los cursos seleccionados en una solicitud', () => {
    service.registrar.and.returnValue(of([matricula]));
    component.nueva(); component.alumnoId = 2; component.cursosIds = [3, 4];
    component.guardar();
    expect(service.registrar).toHaveBeenCalledWith({ alumnoId: 2, cursosIds: [3, 4], fecha: component.hoy });
    expect(component.mostrarFormulario).toBeFalse();
    expect(component.guardando).toBeFalse();
  });
  it('no envía una selección vacía', () => {
    component.alumnoId = 2; component.guardar();
    expect(service.registrar).not.toHaveBeenCalled();
    expect(component.error).toBeTruthy();
  });
  it('mantiene el formulario al rechazar una matrícula duplicada', () => {
    service.registrar.and.returnValue(throwError(() => new HttpErrorResponse({ status: 409, error: { message: 'Matrícula duplicada' } })));
    component.nueva(); component.alumnoId = 2; component.cursosIds = [3]; component.guardar();
    expect(component.error).toBe('Matrícula duplicada');
    expect(component.mostrarFormulario).toBeTrue();
    expect(component.guardando).toBeFalse();
  });
  it('incluye canceladas al detectar cursos previamente matriculados', () => {
    component.alumnoId = 2; component.matriculas = [{ ...matricula, estado: 'CANCELADA' }];
    expect(component.yaMatriculado(3)).toBeTrue();
  });
  it('filtra por alumno o curso y estado', () => {
    component.matriculas = [matricula, { ...matricula, id: 2, estado: 'CANCELADA' }];
    component.busqueda = 'historia'; component.filtroEstado = 'CANCELADA';
    expect(component.filtradas.map(m => m.id)).toEqual([2]);
  });
  it('reactiva mediante edición sin registrar una nueva fila', () => {
    service.actualizar.and.returnValue(of(matricula));
    component.editar({ ...matricula, estado: 'CANCELADA' }); component.estado = 'ACTIVA'; component.guardar();
    expect(service.actualizar).toHaveBeenCalledWith(1, { alumnoId: 2, cursoId: 3, fecha: '2026-01-01', estado: 'ACTIVA' });
    expect(service.registrar).not.toHaveBeenCalled();
  });
});
