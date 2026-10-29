
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

interface PerfilResponse {
  username: string;
  rol: string;
  personaId: number | null;
  nombres: string | null;
  apellidos: string | null;
  correo: string | null;
  telefono: string | null;
  especialidad: string | null;
}

@Component({
  selector: 'app-mi-perfil',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './mi-perfil.html',
  styleUrl: './mi-perfil.css',
})
export class MiPerfil implements OnInit {
  private readonly http = inject(HttpClient);

  perfil: PerfilResponse | null = null;
  cargando = true;
  error = '';

  ngOnInit(): void {
    this.http.get<PerfilResponse>(
      'http://localhost:8080/api/perfil/me'
    ).subscribe({
      next: (respuesta) => {
        this.perfil = respuesta;
        this.cargando = false;
      },
      error: (err) => {
        this.cargando = false;

        if (err.status === 401 || err.status === 403) {
          this.error = 'Tu sesión no es válida. Cierra sesión e inicia nuevamente.';
        } else {
          this.error = 'No se pudo cargar tu perfil. Inténtalo nuevamente.';
        }
      }
    });
  }
}
