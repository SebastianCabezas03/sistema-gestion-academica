import { Routes } from '@angular/router';
import { Dashboard } from './layout/dashboard';
import { Inicio } from './pages/inicio/inicio';
import { Alumnos } from './pages/alumnos/alumnos';
import { Docentes } from './pages/docentes/docentes';
import { CursosComponent } from './pages/admin/cursos/cursos';
import { Login } from './pages/login/login';
import { Matriculas } from './pages/matriculas/matriculas';
import { authGuard } from './guards/auth-guard';
import { MiPerfil } from './pages/mi-perfil/mi-perfil';
import { Cuentas } from './pages/cuentas/cuentas';
import { Calificaciones } from './pages/calificaciones/calificaciones';
import { AsistenciaComponent } from './pages/asistencia/asistencia';
import { RegistroAsistenciaComponent } from './pages/registro-asistencia/registro-asistencia';
import { ReporteAsistenciaComponent } from './pages/reporte-asistencia/reporte-asistencia';

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
      { path: '', redirectTo: 'inicio', pathMatch: 'full' },
      { path: 'inicio', component: Inicio },
      { path: 'alumnos', component: Alumnos },
      { path: 'docentes', component: Docentes },
      { path: 'cursos', component: CursosComponent },
      { path: 'matriculas', component: Matriculas },
      { path: 'calificaciones', component: Calificaciones },
      { path: 'mi-perfil', component: MiPerfil },
      { path: 'asistencia', component: AsistenciaComponent},
      { path: 'registro-asistencia', component: RegistroAsistenciaComponent},
      { path: 'reporte-asistencia', component: ReporteAsistenciaComponent },
      { path: 'cuentas', component: Cuentas },
    ]
  },
  {
    path: '**',
    redirectTo: 'login'
  }
];
