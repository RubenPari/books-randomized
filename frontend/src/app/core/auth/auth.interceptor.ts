import { HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, from, switchMap, throwError } from 'rxjs';
import type { HttpInterceptorFn } from '@angular/common/http';

import { SessionService } from './session.service';

const AUTH_PATHS = ['/api/auth/login', '/api/auth/register', '/api/auth/refresh'] as const;

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const session = inject(SessionService);
  const token = session.accessToken();
  const authenticated = token ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : request;

  return next(authenticated).pipe(
    catchError((error: unknown) => {
      if (!(error instanceof HttpErrorResponse) || error.status !== 401 || AUTH_PATHS.some((path) => path === request.url)) {
        return throwError(() => error);
      }
      return from(session.refresh()).pipe(
        switchMap((freshToken) => {
          if (!freshToken) return throwError(() => error);
          return next(request.clone({ setHeaders: { Authorization: `Bearer ${freshToken}` } }));
        }),
      );
    }),
  );
};
