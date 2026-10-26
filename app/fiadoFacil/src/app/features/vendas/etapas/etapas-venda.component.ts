import { CurrencyPipe } from '@angular/common';
import { Component, computed, inject, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { NovaVendaStore } from '../../../core/services/nova-venda.store';

@Component({
  selector: 'app-etapas-venda',
  imports: [CurrencyPipe, RouterLink],
  templateUrl: './etapas-venda.component.html',
  styleUrl: './etapas-venda.component.css',
})
export class EtapasVendaComponent {
  protected readonly store = inject(NovaVendaStore);

  readonly etapaAtual = input.required<1 | 2>();

  protected readonly quantidadeProdutos = computed(() => this.store.itens().length);

  protected readonly linkProdutos = computed(() => {
    const clienteId = this.store.clienteId();
    const vendaId = this.store.vendaEmEdicao();

    return vendaId
      ? ['/clientes', clienteId, 'vendas', vendaId, 'editar']
      : ['/clientes', clienteId, 'vendas', 'nova'];
  });
}
