import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Observable, forkJoin, finalize } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { Alumno, AlumnoService } from '../../services/alumno';
import { Curso } from '../../models/Curso';
import { CursoService } from '../../services/curso-service';
import { EstadoMatricula, Matricula, MatriculaService } from '../../services/matricula-service';

@Component({
  selector: 'app-matriculas', standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './matriculas.html', styleUrl: './matriculas.css'
})
export class Matriculas implements OnInit {
  private readonly service = inject(MatriculaService);
  private readonly alumnoService = inject(AlumnoService);
  private readonly cursoService = inject(CursoService);
  matriculas: Matricula[] = [];
  alumnos: Alumno[] = [];
  cursos: Curso[] = [];
  cargando = true;
  guardando = false;
  disponible = false;
  error = '';
  mensaje = '';
  mostrarFormulario = false;
  idEditando: number | null = null;
  alumnoId: number | null = null;
  cursosIds: number[] = [];
  cursoId: number | null = null;
  estado: EstadoMatricula = 'ACTIVA';
  readonly hoy = this.fechaLocal();
  fecha = this.hoy;
  busqueda = '';
  filtroEstado = '';

  ngOnInit() { this.cargar(); }
  get filtradas() {
    const texto = this.busqueda.trim().toLocaleLowerCase();
    return this.matriculas.filter(m => (!this.filtroEstado || m.estado === this.filtroEstado) &&
      `${m.nombreAlumno} ${m.nombreCurso}`.toLocaleLowerCase().includes(texto));
  }
  cargar() {
    this.cargando = true;
    this.error = '';
    forkJoin({ matriculas: this.service.listar(), alumnos: this.alumnoService.listar(), cursos: this.cursoService.listar() })
      .pipe(finalize(() => this.cargando = false)).subscribe({
        next: datos => {
          this.matriculas = datos.matriculas; this.alumnos = datos.alumnos; this.cursos = datos.cursos;
          this.disponible = true;
        },
        error: () => { this.disponible = false; this.error = 'No se pudieron cargar las matrículas, alumnos o cursos. Comprueba la conexión y reintenta.'; }
      });
  }
  nueva() {
    this.idEditando = null; this.alumnoId = null; this.cursosIds = []; this.cursoId = null;
    this.fecha = this.hoy; this.estado = 'ACTIVA'; this.error = ''; this.mensaje = '';
    this.mostrarFormulario = true;
  }
  editar(m: Matricula) {
    this.idEditando = m.id; this.alumnoId = m.alumnoId; this.cursoId = m.cursoId;
    this.fecha = m.fecha; this.estado = m.estado; this.error = ''; this.mensaje = '';
    this.mostrarFormulario = true;
  }
  seleccionarCurso(id: number, seleccionado: boolean) {
    this.cursosIds = seleccionado ? [...this.cursosIds, id] : this.cursosIds.filter(c => c !== id);
  }
  yaMatriculado(id: number) {
    return this.matriculas.some(m => m.alumnoId === this.alumnoId && m.cursoId === id);
  }
  guardar() {
    if (this.guardando) return;
    this.error = ''; this.mensaje = '';
    if (!this.alumnoId || !this.fecha || this.fecha > this.hoy ||
      (this.idEditando === null ? !this.cursosIds.length : !this.cursoId)) {
      this.error = 'Selecciona un alumno, al menos un curso y una fecha que no sea futura.'; return;
    }
    this.guardando = true;
    const operacion: Observable<Matricula | Matricula[]> = this.idEditando === null
      ? this.service.registrar({ alumnoId: this.alumnoId, cursosIds: this.cursosIds, fecha: this.fecha })
      : this.service.actualizar(this.idEditando, { alumnoId: this.alumnoId, cursoId: this.cursoId!, fecha: this.fecha, estado: this.estado });
    operacion.pipe(finalize(() => this.guardando = false)).subscribe({
      next: () => { this.mostrarFormulario = false; this.mensaje = 'Matrícula guardada correctamente.'; this.cargar(); },
      error: (err: HttpErrorResponse) => this.error = this.errorServidor(err)
    });
  }
  cancelar(m: Matricula) {
    if (this.guardando || !confirm(`¿Cancelar la matrícula de ${m.nombreAlumno} en ${m.nombreCurso}?`)) return;
    this.guardando = true; this.error = ''; this.mensaje = '';
    this.service.cancelar(m.id).pipe(finalize(() => this.guardando = false)).subscribe({
      next: () => { this.mensaje = 'Matrícula cancelada. Se conserva en el historial.'; this.cargar(); },
      error: (err: HttpErrorResponse) => this.error = this.errorServidor(err)
    });
  }
  private errorServidor(err: HttpErrorResponse) {
    return err.error?.message || (err.status === 400 ? 'Revisa los datos de la matrícula.' : 'No se pudo guardar. Comprueba la conexión e inténtalo nuevamente.');
  }
  private fechaLocal() {
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  }
}
