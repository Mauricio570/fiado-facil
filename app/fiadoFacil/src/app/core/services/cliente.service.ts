import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Cliente, ClienteRequest } from '../models/cliente.model';

@Injectable({ providedIn: 'root' })
export class ClienteService {
  private readonly http = inject(HttpClient);
  private readonly urlBase = `${environment.apiUrl}/clientes`;

  listar(busca?: string): Observable<Cliente[]> {
    const parametros = busca ? new HttpParams().set('busca', busca) : undefined;

    return this.http.get<Cliente[]>(this.urlBase, { params: parametros });
  }

  buscarPorId(id: number): Observable<Cliente> {
    return this.http.get<Cliente>(`${this.urlBase}/${id}`);
  }

  cadastrar(cliente: ClienteRequest): Observable<Cliente> {
    return this.http.post<Cliente>(this.urlBase, cliente);
  }

  editar(id: number, cliente: ClienteRequest): Observable<Cliente> {
    return this.http.put<Cliente>(`${this.urlBase}/${id}`, cliente);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.urlBase}/${id}`);
  }
}
