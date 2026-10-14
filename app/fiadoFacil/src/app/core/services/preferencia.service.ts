import { HttpClient } from '@angular/common/http';
import { DOCUMENT } from '@angular/common';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Preferencia, TamanhoTexto } from '../models/preferencia.model';

@Injectable({ providedIn: 'root' })
export class PreferenciaService {
  private readonly http = inject(HttpClient);
  private readonly documento = inject(DOCUMENT);
  private readonly url = `${environment.apiUrl}/preferencias`;
  private readonly chaveLocal = 'fiadoFacil_tamanho_texto';

  readonly tamanhoTexto = signal<TamanhoTexto>('PADRAO');

  aplicarTamanhoSalvoLocalmente(): void {
    const salvo = localStorage.getItem(this.chaveLocal) as TamanhoTexto | null;

    if (salvo === 'PEQUENO' || salvo === 'PADRAO' || salvo === 'GRANDE') {
      this.aplicar(salvo);
    }
  }

  carregar(): Observable<Preferencia> {
    return this.http
      .get<Preferencia>(this.url)
      .pipe(tap((preferencia) => this.aplicar(preferencia.tamanhoTexto)));
  }

  salvar(tamanhoTexto: TamanhoTexto): Observable<Preferencia> {
    return this.http
      .put<Preferencia>(this.url, { tamanhoTexto })
      .pipe(tap((preferencia) => this.aplicar(preferencia.tamanhoTexto)));
  }

  limpar(): void {
    localStorage.removeItem(this.chaveLocal);
    this.aplicar('PADRAO');
  }

  private aplicar(tamanhoTexto: TamanhoTexto): void {
    this.tamanhoTexto.set(tamanhoTexto);
    localStorage.setItem(this.chaveLocal, tamanhoTexto);
    this.documento.documentElement.dataset['tamanhoTexto'] = tamanhoTexto;
  }
}
