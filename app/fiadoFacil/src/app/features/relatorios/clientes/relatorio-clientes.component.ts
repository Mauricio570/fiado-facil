import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { Subject, catchError, of, startWith, switchMap, tap } from 'rxjs';

import { RelatorioClientes } from '../../../core/models/relatorio.model';
import { RelatorioService } from '../../../core/services/relatorio.service';
import { COR_A_VENCER, COR_EM_ATRASO, COR_PAGO } from '../../../shared/grafico/cores-grafico';
import {
  DestaqueCentral,
  GraficoComponent,
  SerieGrafico,
} from '../../../shared/grafico/grafico.component';
import { mensagemDoErro } from '../../../shared/utils/erro.util';

/** Quantas parcelas da lista de cobrança aparecem antes do "Mostrar mais". */
const PARCELAS_POR_VEZ = 8;

@Component({
  selector: 'app-relatorio-clientes',
  imports: [CurrencyPipe, DatePipe, GraficoComponent, RouterLink],
  templateUrl: './relatorio-clientes.component.html',
  styleUrl: './relatorio-clientes.component.css',
})
export class RelatorioClientesComponent {
  private readonly relatorioService = inject(RelatorioService);
  private readonly recarregar$ = new Subject<void>();

  protected readonly relatorio = signal<RelatorioClientes | null>(null);
  protected readonly carregando = signal(true);
  protected readonly mensagemErro = signal('');
  protected readonly limiteCobranca = signal(PARCELAS_POR_VEZ);

  protected readonly situacao = computed(() => {
    const relatorio = this.relatorio();

    return {
      rotulos: ['Sem débito', 'No prazo', 'Em atraso'],
      series: [
        {
          nome: 'Clientes',
          valores: [
            relatorio?.clientesSemDebito ?? 0,
            relatorio?.clientesNoPrazo ?? 0,
            relatorio?.clientesEmAtraso ?? 0,
          ],
          cor: [COR_PAGO, COR_A_VENCER, COR_EM_ATRASO],
        },
      ] satisfies SerieGrafico[],
      destaque: {
        valor: String(relatorio?.totalClientes ?? 0),
        rotulo: relatorio?.totalClientes === 1 ? 'cliente' : 'clientes',
      } satisfies DestaqueCentral,
    };
  });

  protected readonly devedores = computed(() => {
    const saldos = this.relatorio()?.maioresDevedores ?? [];

    return {
      rotulos: saldos.map((saldo) => saldo.nome),
      series: [
        { nome: 'A vencer', valores: saldos.map((saldo) => saldo.valorAVencer), cor: COR_A_VENCER },
        { nome: 'Em atraso', valores: saldos.map((saldo) => saldo.valorEmAtraso), cor: COR_EM_ATRASO },
      ] satisfies SerieGrafico[],
    };
  });

  protected readonly cobranca = computed(() => {
    const parcelas = this.relatorio()?.parcelasEmAtraso ?? [];

    return {
      visiveis: parcelas.slice(0, this.limiteCobranca()),
      quantidade: parcelas.length,
      total: parcelas.reduce((soma, parcela) => soma + parcela.valor, 0),
      restantes: Math.max(0, parcelas.length - this.limiteCobranca()),
    };
  });

  constructor() {
    this.recarregar$
      .pipe(
        startWith(undefined),
        tap(() => {
          this.carregando.set(true);
          this.mensagemErro.set('');
        }),
        switchMap(() =>
          this.relatorioService.buscarClientes().pipe(
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

  protected mostrarMais(): void {
    this.limiteCobranca.update((limite) => limite + PARCELAS_POR_VEZ);
  }

  protected textoDoAtraso(dias: number): string {
    return dias === 1 ? 'Venceu ontem' : `Venceu há ${dias} dias`;
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
