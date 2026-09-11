import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { Cliente } from '../../../core/models/cliente.model';
import { ClienteService } from '../../../core/services/cliente.service';
import { NovaVendaStore } from '../../../core/services/nova-venda.store';
import { VendaService } from '../../../core/services/venda.service';

@Component({
  selector: 'app-nova-venda',
  imports: [CurrencyPipe, DatePipe, ReactiveFormsModule, RouterLink],
  templateUrl: './nova-venda.component.html',
  styleUrl: './nova-venda.component.css',
})
export class NovaVendaComponent implements OnInit {
  private readonly clienteService = inject(ClienteService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly rotaAtiva = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly vendaService = inject(VendaService);
  protected readonly store = inject(NovaVendaStore);

  protected readonly cliente = signal<Cliente | null>(null);
  protected readonly carregando = signal(true);
  protected readonly mensagemErro = signal('');
  protected readonly modalAberto = signal(false);

  protected readonly formularioItem = this.formBuilder.nonNullable.group({
    nome: ['', [Validators.required, Validators.maxLength(150)]],
    quantidade: [1, [Validators.required, Validators.min(1)]],
    valorUnitario: [null as number | null, [Validators.required, Validators.min(0.01)]],
  });

  private clienteId = 0;

  ngOnInit(): void {
    const parametros = this.rotaAtiva.snapshot.paramMap;
    this.clienteId = Number(parametros.get('id'));
    const vendaId = Number(parametros.get('vendaId'));

    if (vendaId) {
      this.carregarVendaParaEdicao(vendaId);
    } else {
      this.store.iniciarParaCliente(this.clienteId);
    }

    this.clienteService.buscarPorId(this.clienteId).subscribe({
      next: (cliente) => {
        this.cliente.set(cliente);
        this.carregando.set(false);
      },
      error: () => {
        this.mensagemErro.set('Não foi possível carregar os dados do cliente.');
        this.carregando.set(false);
      },
    });
  }

  private carregarVendaParaEdicao(vendaId: number): void {
    // Se o usuário voltou da tela de finalizar, o carrinho já está montado —
    // recarregar apagaria as alterações que ele fez até aqui.
    if (this.store.vendaEmEdicao() === vendaId) {
      return;
    }

    this.vendaService.buscarDetalhe(vendaId).subscribe({
      next: (venda) => {
        this.store.iniciarEdicao(
          this.clienteId,
          venda.id,
          venda.itens.map((item) => ({
            nome: item.nome,
            quantidade: item.quantidade,
            valorUnitario: item.valorUnitario,
          })),
          {
            formaPagamento: venda.pagamento.formaPagamento,
            quantidadeParcelas: venda.pagamento.quantidadeParcelas,
            jurosMes: venda.pagamento.jurosMes,
            valorEntrada: venda.pagamento.valorEntrada,
          },
        );
      },
      error: () => this.mensagemErro.set('Não foi possível carregar a venda para edição.'),
    });
  }

  protected abrirModal(): void {
    this.formularioItem.reset({ nome: '', quantidade: 1, valorUnitario: null });
    this.modalAberto.set(true);
  }

  protected fecharModal(): void {
    this.modalAberto.set(false);
  }

  protected adicionarItem(): void {
    if (this.formularioItem.invalid) {
      this.formularioItem.markAllAsTouched();
      return;
    }

    const { nome, quantidade, valorUnitario } = this.formularioItem.getRawValue();

    this.store.adicionarItem({
      nome: nome.trim(),
      quantidade,
      valorUnitario: Number(valorUnitario),
    });
    this.fecharModal();
  }

  protected alterarQuantidadeDoModal(variacao: number): void {
    const controle = this.formularioItem.controls.quantidade;

    controle.setValue(Math.max(1, controle.value + variacao));
  }

  protected continuar(): void {
    if (this.store.itens().length === 0) {
      this.mensagemErro.set('Adicione ao menos um produto para continuar.');
      return;
    }

    const vendaId = this.store.vendaEmEdicao();
    const destino = vendaId
      ? ['/clientes', this.clienteId, 'vendas', vendaId, 'editar', 'finalizar']
      : ['/clientes', this.clienteId, 'vendas', 'nova', 'finalizar'];

    void this.router.navigate(destino);
  }

  protected iniciais(nome: string): string {
    return nome
      .split(' ')
      .filter((parte) => parte.length > 0)
      .slice(0, 2)
      .map((parte) => parte[0].toUpperCase())
      .join('');
  }
}
