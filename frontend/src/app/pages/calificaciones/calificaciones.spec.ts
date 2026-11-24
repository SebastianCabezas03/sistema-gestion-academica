import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Calificaciones } from './calificaciones';

describe('Calificaciones', () => {
  let http: HttpTestingController;
  const rolAnterior = sessionStorage.getItem('rol');

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [Calificaciones],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    if (rolAnterior === null) sessionStorage.removeItem('rol');
    else sessionStorage.setItem('rol', rolAnterior);
    http.verify();
  });

  it('permite elegir el curso y guardar una nota desde el formulario', async () => {
    sessionStorage.setItem('rol', 'DOCENTE');
    const vista = TestBed.createComponent(Calificaciones);
    vista.detectChanges();
    http.expectOne('/api/docente/asistencia/mis-cursos').flush([{ id: 1, nombreCurso: 'Historia' }]);
    vista.detectChanges();
    await vista.whenStable();

    const selector: HTMLSelectElement = vista.nativeElement.querySelector('select');
    selector.selectedIndex = 1;
    selector.dispatchEvent(new Event('change'));
    http.expectOne('/api/calificaciones/docente/cursos/1').flush([
      { matriculaId: 8, alumno: 'Ana Prueba', nota: null },
    ]);
    vista.detectChanges();
    await vista.whenStable();

    const campo: HTMLInputElement = vista.nativeElement.querySelector('input');
    campo.value = '16.5';
    campo.dispatchEvent(new Event('input'));
    vista.detectChanges();
    vista.nativeElement.querySelector('button').click();
    const guardar = http.expectOne('/api/calificaciones/docente/cursos/1/matriculas/8');
    expect(guardar.request.method).toBe('PUT');
    expect(guardar.request.body).toEqual({ nota: 16.5 });
    guardar.flush({ matriculaId: 8, alumno: 'Ana Prueba', nota: 16.5 });
    vista.detectChanges();
    expect(vista.nativeElement.textContent).toContain('Nota guardada para Ana Prueba');
  });

  it('muestra cero y pendiente como resultados distintos en Mis notas', () => {
    sessionStorage.setItem('rol', 'ALUMNO');
    const vista = TestBed.createComponent(Calificaciones);
    vista.detectChanges();
    http.expectOne('/api/calificaciones/mis-notas').flush([
      { matriculaId: 3, curso: 'Historia', nota: 0 },
      { matriculaId: 4, curso: 'Matemática', nota: null },
    ]);
    vista.detectChanges();
    const filas = vista.nativeElement.querySelectorAll('tbody tr');
    expect(filas[0].cells[1].textContent.trim()).toBe('0');
    expect(filas[1].cells[1].textContent.trim()).toBe('Pendiente');
  });
});
