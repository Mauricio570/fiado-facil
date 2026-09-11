import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Parcela, Venda, VendaRequest, VendaResumo } from '../models/venda.model';

@Injectable({ providedIn: 'root' })
export class VendaService {
  private readonly http = inject(HttpClient);
  private readonly urlBase = `${environment.apiUrl}/vendas`;

  listarPorCliente(clienteId: number): Observable<VendaResumo[]> {
    const parametros = new HttpParams().set('clienteId', clienteId);

    return this.http.get<VendaResumo[]>(this.urlBase, { params: parametros });
  }

  buscarDetalhe(id: number): Observable<Venda> {
    return this.http.get<Venda>(`${this.urlBase}/${id}`);
  }

  cadastrar(venda: VendaRequest): Observable<Venda> {
    return this.http.post<Venda>(this.urlBase, venda);
  }

  editar(id: number, venda: VendaRequest): Observable<Venda> {
    return this.http.put<Venda>(`${this.urlBase}/${id}`, venda);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.urlBase}/${id}`);
  }

  marcarParcelaComoPaga(parcelaId: number): Observable<Parcela> {
    return this.http.patch<Parcela>(`${environment.apiUrl}/parcelas/${parcelaId}/pagar`, {});
  }
}
