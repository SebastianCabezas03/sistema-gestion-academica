import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Docente {
  id?: number;
  nombres: string;
  apellidos: string;
  correo: string;
  telefono: string;
  especialidad: string;
}

@Injectable({
  providedIn: 'root'
})
export class DocenteService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/docentes';

  listar(): Observable<Docente[]> {
    return this.http.get<Docente[]>(this.apiUrl);
  }

  crear(docente: Docente): Observable<Docente> {
    return this.http.post<Docente>(this.apiUrl, docente);
  }

  actualizar(id: number, docente: Docente): Observable<Docente> {
    return this.http.put<Docente>(`${this.apiUrl}/${id}`, docente);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
