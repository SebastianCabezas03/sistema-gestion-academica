import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export type EstadoMatricula = 'ACTIVA' | 'CANCELADA';
export interface Matricula {
  id: number;
  alumnoId: number;
  nombreAlumno: string;
  cursoId: number;
  nombreCurso: string;
  fecha: string;
  estado: EstadoMatricula;
}
export interface MatriculaRegistro { alumnoId: number; cursosIds: number[]; fecha: string; }
export interface MatriculaEdicion { alumnoId: number; cursoId: number; fecha: string; estado: EstadoMatricula; }

@Injectable({ providedIn: 'root' })
export class MatriculaService {
  private readonly http = inject(HttpClient);
  private readonly url = 'http://localhost:8080/api/matriculas';
  listar() { return this.http.get<Matricula[]>(this.url); }
  registrar(dto: MatriculaRegistro) { return this.http.post<Matricula[]>(this.url, dto); }
  actualizar(id: number, dto: MatriculaEdicion) { return this.http.put<Matricula>(`${this.url}/${id}`, dto); }
  cancelar(id: number) { return this.http.patch<Matricula>(`${this.url}/${id}/cancelar`, {}); }
}
