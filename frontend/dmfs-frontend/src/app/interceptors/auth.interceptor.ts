import {
  HttpInterceptorFn
} from '@angular/common/http';

import {
  catchError,
  throwError
} from 'rxjs';

import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn =
  (req, next) => {

    const authService = inject(AuthService);

    const request = req.clone({
      withCredentials: true
    });

    return next(request).pipe(
      catchError(error => {

        if (error?.status === 401) {
          console.error('AUTH 401:', req.url, error);
        }

        return throwError(() => error);
      })
    );
  };
