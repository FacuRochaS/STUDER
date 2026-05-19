import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { TokenStorageService } from './token-storage.service';

/**
 * Guard that redirects authenticated users away from public pages (login, landing).
 * If the user has a valid token, they are redirected to /home.
 */
export const publicGuard: CanActivateFn = () => {
  const tokenStorage = inject(TokenStorageService);
  const router = inject(Router);

  return tokenStorage.hasValidToken()
    ? router.createUrlTree(['/home'])
    : true;
};

