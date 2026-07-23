import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { map, take } from 'rxjs';
import { AuthStateService } from '../../core/auth/auth-state.service';

export const AdminGuard: CanActivateFn = () => {
  const authState = inject(AuthStateService);
  const router = inject(Router);

  return authState.user$.pipe(
    take(1),
    map(user => {
      if (user?.role === 'ADMIN') return true;
      return router.createUrlTree(['/home']);
    }),
  );
};
