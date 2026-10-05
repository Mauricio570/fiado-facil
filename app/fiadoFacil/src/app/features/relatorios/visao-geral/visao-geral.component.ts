import { CurrencyPipe, DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
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

import { RelatorioVisaoGeral } from '../../../core/models/relatorio.model';
import { RelatorioService } from '../../../core/services/relatorio.service';
import { COR_A_VENCER, COR_EM_ATRASO, COR_PAGO } from '../../../shared/grafico/cores-grafico';
import {
  GraficoComponent,
  ItemLegenda,
  SerieGrafico,
} from '../../../shared/grafico/grafico.component';
import { mensagemDoErro } from '../../../shared/utils/erro.util';
import { lerFiltroPeriodo, mesmoPeriodo, rotuloDoMes } from '../periodo-relatorio';

@Component({
  selector: 'app-visao-geral',
  imports: [CurrencyPipe, DecimalPipe, GraficoComponent],
  templateUrl: './visao-geral.component.html',
  styleUrl: './visao-geral.component.css',
})
export class VisaoGeralComponent {
  private readonly relatorioService = inject(RelatorioService);
  private readonly rotaAtiva = inject(ActivatedRoute);
  private readonly recarregar$ = new Subject<void>();

  protected readonly relatorio = signal<RelatorioVisaoGeral | null>(null);
  protected readonly carregando = signal(true);
  protected readonly mensagemErro = signal('');

  protected readonly rotulosDosMeses = computed(
    () => this.relatorio()?.meses.map((mes) => rotuloDoMes(mes.mes)) ?? [],
  );

  /** Cada barra é o vendido do mês, dividido entre pago, a vencer e em atraso. */
  protected readonly seriesVendidoPago = computed<SerieGrafico[]>(() => {
    const meses = this.relatorio()?.meses ?? [];

    return [
      { nome: 'Já pago', valores: meses.map((mes) => mes.valorPago), cor: COR_PAGO },
      { nome: 'A vencer', valores: meses.map((mes) => mes.valorAVencer), cor: COR_A_VENCER },
      { nome: 'Em atraso', valores: meses.map((mes) => mes.valorEmAtraso), cor: COR_EM_ATRASO },
    ];
  });

  protected readonly seriesRecebido = computed<SerieGrafico[]>(() => [
    {
      nome: 'Recebido',
      valores: this.relatorio()?.meses.map((mes) => mes.valorRecebido) ?? [],
      cor: COR_PAGO,
    },
  ]);

  /** A primeira coluna é o que já venceu e não foi pago; as demais, os próximos meses. */
  protected readonly previsao = computed(() => {
    const relatorio = this.relatorio();
    const meses = relatorio?.previsaoRecebimento ?? [];

    return {
      rotulos: ['Em atraso', ...meses.map((mes) => rotuloDoMes(mes.mes))],
      series: [
        {
          nome: 'Previsto',
          valores: [relatorio?.totalEmAtraso ?? 0, ...meses.map((mes) => mes.valor)],
          cor: [COR_EM_ATRASO, ...meses.map(() => COR_A_VENCER)],
        },
      ] satisfies SerieGrafico[],
    };
  });

  protected readonly legendaPrevisao: ItemLegenda[] = [
    { nome: 'Já venceu e não foi pago', cor: COR_EM_ATRASO },
    { nome: 'Vai vencer no mês', cor: COR_A_VENCER },
  ];

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
        // Ao trocar o período, a busca anterior é cancelada e só vale a última.
        switchMap(([periodo]) =>
          this.relatorioService.buscarVisaoGeral(periodo).pipe(
            catchError((erro: unknown) => {
              this.mensagemErro.set(mensagemDoErro(erro, 'Não foi possível carregar o relatório.'));
              return of(null);
            }),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((relatorio) => {
        this.relatorio.set(relatorio);
        this.carregando.set(false);
      });
  }

  protected tentarNovamente(): void {
    this.recarregar$.next();
  }
}
