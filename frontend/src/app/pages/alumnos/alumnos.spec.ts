import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { AlumnoService } from '../../services/alumno';
describe('Listado de alumnos', () => {
  it('consulta la API de alumnos y conserva la respuesta', () => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    const http = TestBed.inject(HttpTestingController);
    let cantidad = -1;
    TestBed.inject(AlumnoService).listar().subscribe(datos => cantidad = datos.length);
    const req = http.expectOne('http://localhost:8080/api/alumnos');
    expect(req.request.method).toBe('GET');
    req.flush([{id: 1, nombres: 'Ana', apellidos: 'Prueba'}]);
    expect(cantidad).toBe(1);
    http.verify();
  });
});
