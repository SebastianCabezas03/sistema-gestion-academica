import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Alumno, AlumnoService } from '../../services/alumno';

@Component({
  selector: 'app-alumnos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './alumnos.html',
  styleUrl: './alumnos.css'
})
export class Alumnos implements OnInit {
  private alumnoService = inject(AlumnoService);

  mostrarConfirmacionEliminar = false;
  alumnoPendienteEliminar: Alumno | null = null;

  alumnos: Alumno[] = [];
  busqueda = '';
  mostrarFormulario = false;
  editando = false;
  cargando = false;
  mensaje = '';
  error = '';

  formulario: Alumno = this.nuevoAlumno();

  ngOnInit(): void {
    this.listarAlumnos();
  }

  nuevoAlumno(): Alumno {
    return {
      nombres: '',
      apellidos: '',
      correo: '',
      telefono: ''
    };
  }

  listarAlumnos(): void {
    this.cargando = true;
    this.error = '';

    this.alumnoService.listar().subscribe({
      next: (datos) => {
        this.alumnos = datos;
        this.cargando = false;
      },
error: (err: HttpErrorResponse) => {
  console.error(err);
  this.error = 'No se pudo completar la operación.';
  this.cargando = false;
}
    });
  }

  get alumnosFiltrados(): Alumno[] {
    const termino = this.busqueda.trim().toLowerCase();

    if (!termino) return this.alumnos;

    return this.alumnos.filter(a =>
      `${a.nombres} ${a.apellidos} ${a.correo ?? ''}`
        .toLowerCase()
        .includes(termino)
    );
  }

  abrirNuevo(): void {
    this.formulario = this.nuevoAlumno();
    this.editando = false;
    this.mostrarFormulario = true;
    this.mensaje = '';
    this.error = '';
  }

  abrirEdicion(alumno: Alumno): void {
    this.formulario = { ...alumno };
    this.editando = true;
    this.mostrarFormulario = true;
    this.mensaje = '';
    this.error = '';
  }

  guardar(): void {
    this.mensaje = '';
    this.error = '';

    const operacion = this.editando && this.formulario.id
      ? this.alumnoService.actualizar(this.formulario.id, this.formulario)
      : this.alumnoService.crear(this.formulario);

    operacion.subscribe({
      next: () => {
        this.mensaje = this.editando
          ? 'Alumno actualizado correctamente.'
          : 'Alumno registrado correctamente.';
        this.mostrarFormulario = false;
        this.listarAlumnos();
      },

  error: (err: HttpErrorResponse) => {
    console.error('Error al guardar alumno:', err);

    if (err.status === 0) {
      this.error = 'No hay conexión con el servidor. Comprueba que Spring Boot esté ejecutándose.';
    } else if (err.status === 400) {
      this.error = 'Los datos enviados no son válidos. Revisa el formulario.';
    } else if (err.status === 409) {
      const correoActual = this.formulario.correo?.trim().toLocaleLowerCase();
      const telefonoActual = this.formulario.telefono?.trim();

      const correoDuplicado = this.alumnos.some(a =>
        a.id !== this.formulario.id &&
        a.correo?.trim().toLocaleLowerCase() === correoActual
      );

      const telefonoDuplicado = this.alumnos.some(a =>
        a.id !== this.formulario.id &&
        a.telefono?.trim() === telefonoActual
      );

      if (telefonoDuplicado) {
        this.error = 'Ese número de teléfono ya está registrado.';
      } else if (correoDuplicado) {
        this.error = 'Ese correo electrónico ya está registrado.';
      } else {
        this.error = 'Ya existe un alumno con esos datos.';
      }
    } else {
      this.error = 'No se pudo guardar el alumno. Inténtalo nuevamente.';
    }
  }

    });
  }


eliminar(alumno: Alumno): void {
  this.alumnoPendienteEliminar = alumno;
  this.mostrarConfirmacionEliminar = true;
}

cancelarEliminacion(): void {
  this.mostrarConfirmacionEliminar = false;
  this.alumnoPendienteEliminar = null;
}

confirmarEliminacion(): void {
  const alumno = this.alumnoPendienteEliminar;

  if (!alumno?.id) {
    return;
  }

  this.mensaje = '';
  this.error = '';

  this.alumnoService.eliminar(alumno.id).subscribe({
    next: () => {
      this.mostrarConfirmacionEliminar = false;
      this.alumnoPendienteEliminar = null;
      this.mensaje = 'Alumno eliminado correctamente.';
      this.listarAlumnos();
    },
    error: (err: HttpErrorResponse) => {
      console.error('Error al eliminar alumno:', err);
      this.mostrarConfirmacionEliminar = false;
      this.alumnoPendienteEliminar = null;
      this.error = 'No se pudo eliminar el alumno. Inténtalo nuevamente.';
    }
  });
}


  cancelar(): void {
    this.mostrarFormulario = false;
    this.error = '';
  }
}
