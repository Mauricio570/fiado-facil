import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import {
  Subject,
  catchError,
  combineLatest,
  distinctUntilChanged,
  map,
  of,
  startWith,
  switchMap,
  tap,
} from 'rxjs';

import { RelatorioVenda } from '../../../core/models/relatorio.model';
import { RelatorioService } from '../../../core/services/relatorio.service';
import { mensagemDoErro } from '../../../shared/utils/erro.util';
import { lerFiltroPeriodo, mesmoPeriodo } from '../periodo-relatorio';

/** Situação da venda vista pelo comerciante: quitada, devendo no prazo ou com atraso. */
type SituacaoVenda = 'QUITADA' | 'NO_PRAZO' | 'EM_ATRASO';

type FiltroSituacao = 'TODAS' | SituacaoVenda;

const VENDAS_POR_PAGINA = 8;

@Component({
  selector: 'app-historico-vendas',
  imports: [CurrencyPipe, DatePipe, FormsModule, RouterLink],
  templateUrl: './historico-vendas.component.html',
  styleUrl: './historico-vendas.component.css',
})
export class HistoricoVendasComponent {
  private readonly relatorioService = inject(RelatorioService);
  private readonly rotaAtiva = inject(ActivatedRoute);
  private readonly recarregar$ = new Subject<void>();

  protected readonly vendas = signal<RelatorioVenda[] | null>(null);
  protected readonly carregando = signal(true);
  protected readonly mensagemErro = signal('');

  protected readonly busca = signal('');
  protected readonly filtroSituacao = signal<FiltroSituacao>('TODAS');
  protected readonly pagina = signal(1);

  protected readonly vendasFiltradas = computed(() => {
    const termo = this.busca().trim().toLowerCase();
    const situacao = this.filtroSituacao();

    return (this.vendas() ?? []).filter(
      (venda) =>
        (!termo || venda.clienteNome.toLowerCase().includes(termo)) &&
        (situacao === 'TODAS' || this.situacao(venda) === situacao),
    );
  });

  /** Números do cabeçalho, sobre todas as vendas do período (antes da busca). */
  protected readonly resumo = computed(() => {
    const vendas = this.vendas() ?? [];

    return {
      quantidade: vendas.length,
      total: vendas.reduce((soma, venda) => soma + venda.valorTotalComJuros, 0),
      quitadas: vendas.filter((venda) => this.situacao(venda) === 'QUITADA').length,
      emAtraso: vendas.filter((venda) => this.situacao(venda) === 'EM_ATRASO').length,
    };
  });

  protected readonly totalPaginas = computed(() =>
    Math.max(1, Math.ceil(this.vendasFiltradas().length / VENDAS_POR_PAGINA)),
  );

  protected readonly vendasDaPagina = computed(() => {
    const inicio = (this.pagina() - 1) * VENDAS_POR_PAGINA;

    return this.vendasFiltradas().slice(inicio, inicio + VENDAS_POR_PAGINA);
  });

  constructor() {
    combineLatest([
      this.rotaAtiva.queryParamMap.pipe(map(lerFiltroPeriodo), distinctUntilChanged(mesmoPeriodo)),
      this.recarregar$.pipe(startWith(undefined)),
    ])
      .pipe(
        tap(() => {
          this.carregando.set(true);
          this.mensagemErro.set('');
        }),
        switchMap(([periodo]) =>
          this.relatorioService.listarVendas(periodo).pipe(
            catchError((erro: unknown) => {
              this.mensagemErro.set(
                mensagemDoErro(erro, 'Não foi possível carregar o histórico de vendas.'),
              );
              return of(null);
            }),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((vendas) => {
        this.vendas.set(vendas);
        this.pagina.set(1);
        this.carregando.set(false);
      });
  }

  protected tentarNovamente(): void {
    this.recarregar$.next();
  }

  protected alterarBusca(termo: string): void {
    this.busca.set(termo);
    this.pagina.set(1);
  }

  protected alterarSituacao(situacao: FiltroSituacao): void {
    this.filtroSituacao.set(situacao);
    this.pagina.set(1);
  }

  protected irParaPagina(pagina: number): void {
    this.pagina.set(Math.min(Math.max(pagina, 1), this.totalPaginas()));
  }

  protected situacao(venda: RelatorioVenda): SituacaoVenda {
    if (venda.status === 'PAGO') {
      return 'QUITADA';
    }

    return venda.valorEmAtraso > 0 ? 'EM_ATRASO' : 'NO_PRAZO';
  }

  protected percentualPago(venda: RelatorioVenda): number {
    return venda.valorTotalComJuros > 0
      ? Math.round((venda.valorPago / venda.valorTotalComJuros) * 100)
      : 100;
  }

  protected formaDePagamento(venda: RelatorioVenda): string {
    return venda.formaPagamento === 'A_VISTA'
      ? 'À vista'
      : `Crédito em ${venda.quantidadeParcelas}x`;
  }
}
