import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Painel } from '../models/painel.model';

@Injectable({ providedIn: 'root' })
export class PainelService {
  private readonly http = inject(HttpClient);

  buscarResumo(): Observable<Painel> {
    return this.http.get<Painel>(`${environment.apiUrl}/painel`);
  }
}
