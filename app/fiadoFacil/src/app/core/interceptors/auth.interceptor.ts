import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (requisicao, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const rotasPublicas = [`${environment.apiUrl}/auth/login`, `${environment.apiUrl}/usuarios`];

  if (!requisicao.url.startsWith(environment.apiUrl) || rotasPublicas.includes(requisicao.url)) {
    return next(requisicao);
  }

  const token = authService.getToken();
  const requisicaoAutenticada = token
    ? requisicao.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : requisicao;

  return next(requisicaoAutenticada).pipe(
    catchError((erro: HttpErrorResponse) => {
      if (erro.status === 401) {
        authService.logout();
        void router.navigate(['/login']);
      }

      return throwError(() => erro);
    }),
  );
};
