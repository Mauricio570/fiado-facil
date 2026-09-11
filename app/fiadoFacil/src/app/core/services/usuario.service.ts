import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Usuario, UsuarioAtualizacaoRequest } from '../models/usuario.model';

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private readonly http = inject(HttpClient);
  private readonly urlBase = `${environment.apiUrl}/usuarios`;

  buscarLogado(): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.urlBase}/me`);
  }

  atualizarLogado(dados: UsuarioAtualizacaoRequest): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.urlBase}/me`, dados);
  }
}
