import { Injectable, computed, signal } from '@angular/core';

import { ItemVendaRequest, PagamentoRequest } from '../models/venda.model';

/**
 * Guarda os itens da venda em andamento enquanto o usuário navega entre a
 * tela "Nova venda" e a tela "Finalizar venda". Como o carrinho só existe
 * no navegador até o POST/PUT em /api/vendas, o estado vive em memória.
 *
 * O mesmo fluxo atende o cadastro e a edição: quando `vendaEmEdicao` está
 * preenchido, a tela de finalizar chama o PUT em vez do POST.
 */
@Injectable({ providedIn: 'root' })
export class NovaVendaStore {
  private readonly _clienteId = signal<number | null>(null);
  private readonly _vendaEmEdicao = signal<number | null>(null);
  private readonly _itens = signal<ItemVendaRequest[]>([]);
  private readonly _pagamentoOriginal = signal<PagamentoRequest | null>(null);

  readonly clienteId = this._clienteId.asReadonly();
  readonly vendaEmEdicao = this._vendaEmEdicao.asReadonly();
  readonly itens = this._itens.asReadonly();
  readonly pagamentoOriginal = this._pagamentoOriginal.asReadonly();

  readonly valorTotal = computed(() =>
    this._itens().reduce((total, item) => total + item.quantidade * item.valorUnitario, 0),
  );

  /** Zera o carrinho quando o usuário começa uma venda nova para um cliente. */
  iniciarParaCliente(clienteId: number): void {
    if (this._clienteId() !== clienteId || this._vendaEmEdicao() !== null) {
      this._clienteId.set(clienteId);
      this._vendaEmEdicao.set(null);
      this._pagamentoOriginal.set(null);
      this._itens.set([]);
    }
  }

  /** Carrega uma venda existente no carrinho para edição. */
  iniciarEdicao(
    clienteId: number,
    vendaId: number,
    itens: ItemVendaRequest[],
    pagamento: PagamentoRequest,
  ): void {
    this._clienteId.set(clienteId);
    this._vendaEmEdicao.set(vendaId);
    this._itens.set(itens);
    this._pagamentoOriginal.set(pagamento);
  }

  adicionarItem(item: ItemVendaRequest): void {
    this._itens.update((itens) => [...itens, item]);
  }

  removerItem(indice: number): void {
    this._itens.update((itens) => itens.filter((_, posicao) => posicao !== indice));
  }

  alterarQuantidade(indice: number, variacao: number): void {
    this._itens.update((itens) =>
      itens.map((item, posicao) =>
        posicao === indice
          ? { ...item, quantidade: Math.max(1, item.quantidade + variacao) }
          : item,
      ),
    );
  }

  limpar(): void {
    this._clienteId.set(null);
    this._vendaEmEdicao.set(null);
    this._pagamentoOriginal.set(null);
    this._itens.set([]);
  }
}
