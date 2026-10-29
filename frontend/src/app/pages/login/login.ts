import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  username = '';
  password = '';
  cargando = false;
  error = '';

  private readonly apiUrl = 'http://localhost:8080/api/auth/login';

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  iniciarSesion(): void {
    this.error = '';

    if (!this.username.trim() || !this.password) {
      this.error = 'Ingresa tu usuario y contraseña.';
      return;
    }

    this.cargando = true;

    this.http.post<{
      username: string;
      mensaje: string;
      rol: string;
      token: string;
    }>(
      this.apiUrl,
      {
        username: this.username.trim(),
        password: this.password
      }
    ).subscribe({
      next: (respuesta) => {
        this.cargando = false;

        if (!['ADMIN', 'DOCENTE', 'ALUMNO'].includes(respuesta.rol)) {
          this.error = 'Usuario o contraseña incorrectos.';
          return;
        }

        if (!respuesta.token) {
          this.error = 'El servidor no devolvió el token de autenticación.';
          return;
        }

        sessionStorage.setItem('usuario', respuesta.username);
        sessionStorage.setItem('rol', respuesta.rol);
        sessionStorage.setItem('token', respuesta.token);

        if (respuesta.rol === 'ADMIN') {
          this.router.navigate(['/inicio']);
        } else {
          this.router.navigate(['/mi-perfil']);
        }
      },
      error: (err) => {
        this.cargando = false;

        if (err.status === 401 || err.status === 403) {
          this.error = 'Usuario o contraseña incorrectos.';
        } else {
          this.error = 'No se pudo conectar con el servidor. Verifica que el backend esté iniciado.';
        }
      }
    });
  }
}
