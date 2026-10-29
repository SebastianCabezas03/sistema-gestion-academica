import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { Inicio } from './inicio';
import { AlumnoService } from '../../services/alumno';
import { DocenteService } from '../../services/docentes';
import { CursoService } from '../../services/curso-service';
import { MatriculaService } from '../../services/matricula-service';
describe('Estadísticas de inicio', () => {
  function configure(fail = false) {
    TestBed.configureTestingModule({ imports: [Inicio], providers: [provideRouter([]),
      {provide: AlumnoService, useValue: {listar: () => fail ? throwError(() => new Error('offline')) : of([{id:1,nombres:'Ana',apellidos:'Prueba',correo:'ana@example.test'}])}},
      {provide: DocenteService, useValue: {listar: () => of([{},{}])}},
      {provide: CursoService, useValue: {listar: () => of([{},{},{}])}},
      {provide: MatriculaService, useValue: {listar: () => of([{estado:'ACTIVA'},{estado:'CANCELADA'}])}}
    ]});
    const f = TestBed.createComponent(Inicio);
    f.detectChanges();
    return f;
  }
  it('muestra los cuatro totales incluyendo matrículas canceladas', () => {
    const f = configure();
    expect(f.componentInstance.alumnos.length).toBe(1);
    expect(f.componentInstance.totalDocentes).toBe(2);
    expect(f.componentInstance.totalCursos).toBe(3);
    expect(f.componentInstance.totalMatriculas).toBe(2);
    expect(f.nativeElement.textContent).not.toContain('Módulo pendiente');
  });
  it('mantiene los otros totales si alumnos falla', () => {
    const f = configure(true);
    expect(f.componentInstance.errorAlumnos).toBeTrue();
    expect(f.componentInstance.totalDocentes).toBe(2);
    expect(f.componentInstance.totalCursos).toBe(3);
    expect(f.componentInstance.totalMatriculas).toBe(2);
    expect(f.nativeElement.textContent).toContain('No se pudo cargar');
  });
});
