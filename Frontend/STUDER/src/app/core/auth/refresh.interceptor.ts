import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthStateService } from './auth-state.service';
import { catchError, switchMap, throwError } from 'rxjs';

export const refreshInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError(err => {
      if (err.status === 401) {
        const authState = inject(AuthStateService);
        return authState.refresh().pipe(
          switchMap(success => {
            if (success) {
              return next(req);
            } else {
              return throwError(() => err);
            }
          })
        );
      }
      return throwError(() => err);
    })
  );
};

