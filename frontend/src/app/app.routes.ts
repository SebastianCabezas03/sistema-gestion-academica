import { Routes } from '@angular/router';
import { Dashboard } from './layout/dashboard';
import { Inicio } from './pages/inicio/inicio';
import { Alumnos } from './pages/alumnos/alumnos';
import { Docentes } from './pages/docentes/docentes';
import { CursosComponent } from './pages/admin/cursos/cursos';
import { Login } from './pages/login/login';
import { Matriculas } from './pages/matriculas/matriculas';
import { authGuard } from './guards/auth-guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  },
  {
    path: 'login',
    component: Login
  },
  {
    path: '',
    component: Dashboard,
    canActivate: [authGuard],
    children: [
      {
        path: '',
        redirectTo: 'inicio',
        pathMatch: 'full'
      },
      {
        path: 'inicio',
        component: Inicio
      },
      {
        path: 'alumnos',
        component: Alumnos
      },
      {
        path: 'docentes',
        component: Docentes
      },
      {
        path: 'cursos',
        component: CursosComponent
      },
      { path: 'matriculas', component: Matriculas }
    ]
  },
  {
    path: '**',
    redirectTo: 'login'
  }
];
