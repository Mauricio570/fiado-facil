import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { map, Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly chaveToken = 'fiadoFacil_token';
  private readonly chaveExpiracao = 'fiadoFacil_token_expira_em';

  login(credenciais: LoginRequest): Observable<void> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, credenciais).pipe(
      map((resposta) => {
        const expiraEm = Date.now() + resposta.expiraEmSegundos * 1000;

        localStorage.setItem(this.chaveToken, resposta.token);
        localStorage.setItem(this.chaveExpiracao, String(expiraEm));
      }),
    );
  }

  getToken(): string | null {
    if (!this.estaAutenticado()) {
      return null;
    }

    return localStorage.getItem(this.chaveToken);
  }

  estaAutenticado(): boolean {
    const token = localStorage.getItem(this.chaveToken);
    const expiraEm = Number(localStorage.getItem(this.chaveExpiracao));

    if (!token || !expiraEm || Date.now() >= expiraEm) {
      this.logout();
      return false;
    }

    return true;
  }

  logout(): void {
    localStorage.removeItem(this.chaveToken);
    localStorage.removeItem(this.chaveExpiracao);
  }
}
