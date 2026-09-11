import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { map, Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse } from '../models/auth.model';
import { UsuarioLogado } from '../models/usuario.model';

interface PayloadToken {
  sub: string;
  email: string;
  nomeEmpresa: string;
}

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

  /** Lê os dados do usuário direto das claims do JWT — a API não expõe um endpoint /me. */
  getUsuarioLogado(): UsuarioLogado | null {
    const token = this.getToken();

    if (!token) {
      return null;
    }

    const payload = this.lerPayload(token);

    if (!payload) {
      return null;
    }

    return { id: Number(payload.sub), email: payload.email, nomeEmpresa: payload.nomeEmpresa };
  }

  logout(): void {
    localStorage.removeItem(this.chaveToken);
    localStorage.removeItem(this.chaveExpiracao);
  }

  private lerPayload(token: string): PayloadToken | null {
    const partes = token.split('.');

    if (partes.length !== 3) {
      return null;
    }

    try {
      const base64 = partes[1].replace(/-/g, '+').replace(/_/g, '/');
      const binario = atob(base64);
      const json = decodeURIComponent(
        Array.from(binario, (caractere) => {
          const codigo = caractere.charCodeAt(0).toString(16).padStart(2, '0');
          return `%${codigo}`;
        }).join(''),
      );

      return JSON.parse(json) as PayloadToken;
    } catch {
      return null;
    }
  }
}
