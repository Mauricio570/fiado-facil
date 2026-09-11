import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { PreferenciaService } from './core/services/preferencia.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  private readonly preferenciaService = inject(PreferenciaService);

  constructor() {
    // Aplica o último tamanho conhecido antes da primeira pintura, para o
    // texto não aparecer no tamanho padrão e mudar depois que a API responde.
    this.preferenciaService.aplicarTamanhoSalvoLocalmente();
  }
}
