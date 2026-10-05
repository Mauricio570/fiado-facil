import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  PeriodoRelatorio,
  RelatorioClientes,
  RelatorioVenda,
  RelatorioVisaoGeral,
} from '../models/relatorio.model';

@Injectable({ providedIn: 'root' })
export class RelatorioService {
  private readonly http = inject(HttpClient);
  private readonly urlBase = `${environment.apiUrl}/relatorios`;

  buscarVisaoGeral(periodo: PeriodoRelatorio): Observable<RelatorioVisaoGeral> {
    return this.http.get<RelatorioVisaoGeral>(`${this.urlBase}/visao-geral`, {
      params: this.parametros(periodo),
    });
  }

  /** Situação de hoje — não depende do período. */
  buscarClientes(): Observable<RelatorioClientes> {
    return this.http.get<RelatorioClientes>(`${this.urlBase}/clientes`);
  }

  listarVendas(periodo: PeriodoRelatorio): Observable<RelatorioVenda[]> {
    return this.http.get<RelatorioVenda[]>(`${this.urlBase}/vendas`, {
      params: this.parametros(periodo),
    });
  }

  private parametros(periodo: PeriodoRelatorio): HttpParams {
    return new HttpParams().set('inicio', periodo.inicio).set('fim', periodo.fim);
  }
}
