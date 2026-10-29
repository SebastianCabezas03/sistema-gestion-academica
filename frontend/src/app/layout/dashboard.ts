import { Component } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard {
  menuCollapsed = false;

  usuario = sessionStorage.getItem('usuario') ?? '';
  rol = sessionStorage.getItem('rol') ?? '';

  constructor(private router: Router) {}

  toggleMenu(): void {
    this.menuCollapsed = !this.menuCollapsed;
  }

  cerrarSesion(): void {
    sessionStorage.removeItem('usuario');
    sessionStorage.removeItem('rol');
    this.router.navigate(['/login']);
  }

  get nombreRol(): string {
    switch (this.rol) {
      case 'ADMIN': return 'Administrador';
      case 'DOCENTE': return 'Docente';
      case 'ALUMNO': return 'Alumno';
      default: return 'Usuario';
    }
  }

  get iniciales(): string {
    return this.usuario.substring(0, 2).toUpperCase();
  }
}
