import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { firstValueFrom, filter } from 'rxjs';
import { AuthStateService } from '../../core/auth/auth-state.service';

export const AdminGuard: CanActivateFn = async () => {
  const authState = inject(AuthStateService);
  const router = inject(Router);

  const user = await firstValueFrom(authState.user$.pipe(
    filter(u => u !== null)
  ));
  if (user?.role == 'ADMIN') return true;
  return router.createUrlTree(['/home']);
};
