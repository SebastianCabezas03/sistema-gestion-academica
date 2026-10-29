import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AlumnoService, Alumno } from '../../services/alumno';
import { DocenteService } from '../../services/docentes';
import { CursoService } from '../../services/curso-service';
import { MatriculaService } from '../../services/matricula-service';

@Component({
  selector: 'app-inicio', standalone: true, imports: [RouterLink],
  templateUrl: './inicio.html', styleUrl: './inicio.css'
})
export class Inicio implements OnInit {
  private readonly alumnoService = inject(AlumnoService);
  private readonly docenteService = inject(DocenteService);
  private readonly cursoService = inject(CursoService);
  private readonly matriculaService = inject(MatriculaService);
  alumnos: Alumno[] = [];
  totalDocentes: number | null = null;
  totalCursos: number | null = null;
  totalMatriculas: number | null = null;
  cargandoAlumnos = true;
  errorAlumnos = false;
  cargandoDocentes = true;
  cargandoCursos = true;
  cargandoMatriculas = true;
  get alumnosRecientes(): Alumno[] { return this.alumnos.slice(-5).reverse(); }
  ngOnInit(): void {
    this.alumnoService.listar().subscribe({
      next: alumnos => { this.alumnos = alumnos; this.cargandoAlumnos = false; },
      error: () => { this.errorAlumnos = true; this.cargandoAlumnos = false; }
    });
    this.docenteService.listar().subscribe({
      next: docentes => { this.totalDocentes = docentes.length; this.cargandoDocentes = false; },
      error: () => { this.cargandoDocentes = false; }
    });
    this.cursoService.listar().subscribe({
      next: cursos => { this.totalCursos = cursos.length; this.cargandoCursos = false; },
      error: () => { this.cargandoCursos = false; }
    });
    this.matriculaService.listar().subscribe({
      next: matriculas => { this.totalMatriculas = matriculas.length; this.cargandoMatriculas = false; },
      error: () => { this.cargandoMatriculas = false; }
    });
  }
}
