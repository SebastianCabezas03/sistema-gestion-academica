import { HttpClient } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

interface Curso {
  id: number;
  nombreCurso: string;
}

interface NotaDocente {
  matriculaId: number;
  alumno: string;
  nota: number | null;
}

interface NotaAlumno {
  matriculaId: number;
  curso: string;
  nota: number | null;
}

@Component({
  selector: 'app-calificaciones',
  imports: [FormsModule],
  templateUrl: './calificaciones.html',
  styleUrl: './calificaciones.css',
})
export class Calificaciones implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/calificaciones';
  readonly rol = sessionStorage.getItem('rol');
  readonly cursos = signal<Curso[]>([]);
  readonly alumnos = signal<NotaDocente[]>([]);
  readonly notas = signal<NotaAlumno[]>([]);
  readonly cargando = signal(false);
  readonly guardando = signal<number | null>(null);
  readonly mensaje = signal('');
  readonly error = signal('');
  cursoId: number | null = null;

  ngOnInit(): void {
    if (this.rol === 'DOCENTE') {
      this.cargar('/api/docente/asistencia/mis-cursos', this.cursos);
    } else if (this.rol === 'ALUMNO') {
      this.cargar(this.api + '/mis-notas', this.notas);
    }
  }

  seleccionarCurso(cursoId: number | null): void {
    this.cursoId = cursoId;
    this.alumnos.set([]);
    this.error.set('');
    this.mensaje.set('');
    if (cursoId !== null) {
      this.cargar(this.api + '/docente/cursos/' + cursoId, this.alumnos);
    }
  }

  guardar(fila: NotaDocente, nota: number | null): void {
    if (nota === null || this.cursoId === null || this.guardando() !== null) {
      return;
    }

    this.guardando.set(fila.matriculaId);
    this.mensaje.set('');
    this.error.set('');
    const url = this.api + '/docente/cursos/' + this.cursoId + '/matriculas/' + fila.matriculaId;
    this.http.put<NotaDocente>(url, { nota }).subscribe({
      next: actualizada => {
        this.alumnos.update(filas => filas.map(actual =>
          actual.matriculaId === actualizada.matriculaId ? actualizada : actual
        ));
        this.mensaje.set('Nota guardada para ' + actualizada.alumno + '.');
        this.guardando.set(null);
      },
      error: () => {
        this.error.set('No se pudo guardar la nota. Verifica la matrícula y la escala de 0 a 20.');
        this.guardando.set(null);
      },
    });
  }

  private cargar<T>(url: string, destino: { set: (valor: T[]) => void }): void {
    this.cargando.set(true);
    this.http.get<T[]>(url).subscribe({
      next: datos => {
        destino.set(datos);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar los datos de calificaciones.');
        this.cargando.set(false);
      },
    });
  }
}
