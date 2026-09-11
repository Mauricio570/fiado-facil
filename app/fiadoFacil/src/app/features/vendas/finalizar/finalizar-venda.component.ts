import { CurrencyPipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { FormaPagamento } from '../../../core/models/venda.model';
import { NovaVendaStore } from '../../../core/services/nova-venda.store';
import { VendaService } from '../../../core/services/venda.service';
import { mensagemDoErro } from '../../../shared/utils/erro.util';

@Component({
  selector: 'app-finalizar-venda',
  imports: [CurrencyPipe, ReactiveFormsModule, RouterLink],
  templateUrl: './finalizar-venda.component.html',
  styleUrl: './finalizar-venda.component.css',
})
export class FinalizarVendaComponent implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly vendaService = inject(VendaService);
  private readonly rotaAtiva = inject(ActivatedRoute);
  private readonly router = inject(Router);
  protected readonly store = inject(NovaVendaStore);

  protected readonly salvando = signal(false);
  protected readonly mensagemErro = signal('');

  protected readonly formulario = this.formBuilder.nonNullable.group({
    formaPagamento: 'CREDITO' as FormaPagamento,
    quantidadeParcelas: 1,
    jurosMes: 0,
    valorEntrada: 0,
  });

  private readonly valores = toSignal(this.formulario.valueChanges, {
    initialValue: this.formulario.getRawValue(),
  });

  /**
   * Repete a mesma regra de juros simples do back-end
   * (VendaService.processarPagamento) para que o resumo mostrado aqui
   * bata exatamente com o que será gravado.
   */
  protected readonly ehCredito = computed(() => this.valores().formaPagamento !== 'A_VISTA');

  protected readonly resumo = computed(() => {
    const { formaPagamento, quantidadeParcelas, jurosMes, valorEntrada } = this.valores();
    const total = this.store.valorTotal();

    if (formaPagamento === 'A_VISTA') {
      return {
        total,
        valorComJuros: total,
        entrada: total,
        faltaPagar: 0,
        quantidadeParcelas: 1,
        jurosMes: 0,
        valorParcela: 0,
      };
    }

    const parcelas = Math.max(1, Number(quantidadeParcelas) || 1);
    const juros = Number(jurosMes) || 0;
    const entrada = Math.min(Math.max(0, Number(valorEntrada) || 0), total);

    const financiado = total - entrada;
    const valorFinal = this.arredondar(financiado * (1 + (juros / 100) * parcelas));
    const valorParcela = Math.floor((valorFinal / parcelas) * 100) / 100;

    return {
      total,
      valorComJuros: this.arredondar(entrada + valorFinal),
      entrada,
      faltaPagar: valorFinal,
      quantidadeParcelas: parcelas,
      jurosMes: juros,
      valorParcela,
    };
  });

  private clienteId = 0;

  ngOnInit(): void {
    this.clienteId = Number(this.rotaAtiva.snapshot.paramMap.get('id'));

    // Sem itens no carrinho não há o que finalizar — volta para a montagem da venda.
    if (this.store.itens().length === 0) {
      void this.router.navigate(['/clientes', this.clienteId, 'vendas', 'nova']);
      return;
    }

    // Ao editar, o formulário já abre com a forma de pagamento atual da venda.
    const pagamento = this.store.pagamentoOriginal();

    if (pagamento) {
      this.formulario.setValue({
        formaPagamento: pagamento.formaPagamento,
        quantidadeParcelas: pagamento.quantidadeParcelas ?? 1,
        jurosMes: pagamento.jurosMes ?? 0,
        valorEntrada: pagamento.valorEntrada ?? 0,
      });
    }
  }

  protected finalizar(): void {
    const dados = this.resumo();
    const aVista = !this.ehCredito();

    if (!aVista && dados.entrada > dados.total) {
      this.mensagemErro.set('O valor de entrada não pode ser maior que o total da compra.');
      return;
    }

    this.mensagemErro.set('');
    this.salvando.set(true);

    const requisicao = {
      fkCliente: this.clienteId,
      itens: this.store.itens(),
      pagamento: {
        formaPagamento: aVista ? ('A_VISTA' as const) : ('CREDITO' as const),
        quantidadeParcelas: aVista ? null : dados.quantidadeParcelas,
        jurosMes: aVista ? null : dados.jurosMes,
        valorEntrada: aVista ? null : dados.entrada,
      },
    };

    const vendaId = this.store.vendaEmEdicao();
    const operacao = vendaId
      ? this.vendaService.editar(vendaId, requisicao)
      : this.vendaService.cadastrar(requisicao);

    operacao.pipe(finalize(() => this.salvando.set(false))).subscribe({
      next: () => {
        this.store.limpar();
        void this.router.navigate(['/clientes', this.clienteId]);
      },
      error: (erro) =>
        this.mensagemErro.set(
          mensagemDoErro(
            erro,
            vendaId ? 'Não foi possível salvar a venda.' : 'Não foi possível registrar a venda.',
          ),
        ),
    });
  }

  private arredondar(valor: number): number {
    return Math.round((valor + Number.EPSILON) * 100) / 100;
  }
}
