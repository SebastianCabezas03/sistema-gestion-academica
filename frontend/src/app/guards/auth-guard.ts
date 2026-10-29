import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';

export const authGuard: CanActivateFn = () => {
  const router = inject(Router);

  const usuario = sessionStorage.getItem('usuario');
  const rol = sessionStorage.getItem('rol');

  const rolesPermitidos = ['ADMIN', 'DOCENTE', 'ALUMNO'];

  if (usuario && rol && rolesPermitidos.includes(rol)) {
    return true;
  }

  return router.createUrlTree(['/login']);
};
