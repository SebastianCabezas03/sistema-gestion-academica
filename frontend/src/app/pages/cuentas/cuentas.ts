
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Alumno, AlumnoService } from '../../services/alumno';
import { Docente, DocenteService } from '../../services/docentes';

interface RespuestaCuenta {
  mensaje: string;
  username: string;
  rol: string;
}

@Component({
  selector: 'app-cuentas',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cuentas.html',
  styleUrl: './cuentas.css'
})
export class Cuentas implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly alumnoService = inject(AlumnoService);
  private readonly docenteService = inject(DocenteService);

  alumnos: Alumno[] = [];
  docentes: Docente[] = [];

  rol = 'ALUMNO';
  personaId: number | null = null;
  username = '';
  password = '';

  cargando = false;
  guardando = false;
  error = '';
  mensaje = '';

  ngOnInit(): void {
    this.cargarPersonas();
  }

  cargarPersonas(): void {
  this.cargando = true;
  this.error = '';

  this.alumnoService.listar().subscribe({
    next: (datos: Alumno[]) => {
      this.alumnos = datos;
      this.cargando = false;
    },
    error: () => {
      this.error = 'No se pudieron cargar los alumnos.';
      this.cargando = false;
    }
  });

  this.docenteService.listar().subscribe({
    next: (datos: Docente[]) => {
      this.docentes = datos;
    },
    error: () => {
      this.error = 'No se pudieron cargar los docentes.';
    }
  });
}


  cambiarRol(): void {
    this.personaId = null;
    this.error = '';
    this.mensaje = '';
  }

  crearCuenta(): void {
    this.error = '';
    this.mensaje = '';

    if (!this.username.trim() || !this.password.trim()) {
      this.error = 'Completa el usuario y la contraseña temporal.';
      return;
    }

    if (this.password.length < 8 || this.password.length > 72) {
      this.error = 'La contraseña debe tener entre 8 y 72 caracteres.';
      return;
    }

    if (this.personaId === null) {
      this.error = 'Selecciona un alumno o docente.';
      return;
    }

    const datos: {
      username: string;
      password: string;
      rol: string;
      alumnoId?: number;
      docenteId?: number;
    } = {
      username: this.username.trim(),
      password: this.password,
      rol: this.rol
    };

    if (this.rol === 'ALUMNO') {
      datos.alumnoId = this.personaId;
    } else {
      datos.docenteId = this.personaId;
    }

    this.guardando = true;

    this.http.post<RespuestaCuenta>(
      'http://localhost:8080/api/cuentas',
      datos
    ).subscribe({
      next: (respuesta) => {
        this.guardando = false;
        this.mensaje = `${respuesta.mensaje}. Usuario: ${respuesta.username}`;
        this.username = '';
        this.password = '';
        this.personaId = null;
      },
      error: (err) => {
        this.guardando = false;

        this.error = err.error?.detail
          || err.error?.message
          || err.error?.mensaje
          || 'No se pudo crear la cuenta. Verifica los datos e inténtalo nuevamente.';
      }
    });
  }
}
