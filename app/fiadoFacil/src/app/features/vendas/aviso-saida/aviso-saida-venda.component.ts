import { CurrencyPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';

import { NovaVendaStore } from '../../../core/services/nova-venda.store';

@Component({
  selector: 'app-aviso-saida-venda',
  imports: [CurrencyPipe],
  templateUrl: './aviso-saida-venda.component.html',
})
export class AvisoSaidaVendaComponent {
  protected readonly store = inject(NovaVendaStore);

  protected readonly aberto = signal(false);

  private responder: ((sair: boolean) => void) | null = null;

  confirmar(): Promise<boolean> {
    this.responder?.(false);
    this.aberto.set(true);

    return new Promise((resolve) => (this.responder = resolve));
  }

  protected continuarNaVenda(): void {
    this.fechar(false);
  }

  protected sairEDescartar(): void {
    this.fechar(true);
  }

  private fechar(sair: boolean): void {
    this.aberto.set(false);
    this.responder?.(sair);
    this.responder = null;
  }
}
