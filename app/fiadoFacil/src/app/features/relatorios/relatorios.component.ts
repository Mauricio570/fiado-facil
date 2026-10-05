import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import {
  ActivatedRoute,
  NavigationEnd,
  Params,
  Router,
  RouterLink,
  RouterLinkActive,
  RouterOutlet,
} from '@angular/router';
import { filter, map } from 'rxjs';

import { ATALHOS_DE_MESES, AtalhoDeMeses, lerFiltroPeriodo } from './periodo-relatorio';

/**
 * Casca da tela de relatórios: cabeçalho, abas e o filtro de período. Cada
 * aba é uma rota filha que lê o período da URL e busca os próprios dados.
 */
@Component({
  selector: 'app-relatorios',
  imports: [DatePipe, FormsModule, RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './relatorios.component.html',
  styleUrl: './relatorios.component.css',
})
export class RelatoriosComponent {
  private readonly router = inject(Router);
  private readonly rotaAtiva = inject(ActivatedRoute);

  protected readonly atalhos = ATALHOS_DE_MESES;

  protected readonly filtro = toSignal(this.rotaAtiva.queryParamMap.pipe(map(lerFiltroPeriodo)), {
    requireSync: true,
  });

  /** A aba Clientes mostra a situação de hoje, então o filtro de período some nela. */
  protected readonly abaUsaPeriodo = toSignal(
    this.router.events.pipe(
      filter((evento) => evento instanceof NavigationEnd),
      map(() => this.usaPeriodo()),
    ),
    { initialValue: this.usaPeriodo() },
  );

  protected readonly editandoIntervalo = signal(false);
  protected readonly inicioDigitado = signal('');
  protected readonly fimDigitado = signal('');

  protected selecionarAtalho(meses: AtalhoDeMeses): void {
    this.editandoIntervalo.set(false);
    this.navegar({ meses });
  }

  protected abrirIntervalo(): void {
    const atual = this.filtro();

    this.inicioDigitado.set(atual.inicio);
    this.fimDigitado.set(atual.fim);
    this.editandoIntervalo.set(true);
  }

  protected aplicarIntervalo(): void {
    this.navegar({ inicio: this.inicioDigitado(), fim: this.fimDigitado() });
    this.editandoIntervalo.set(false);
  }

  // Sem comandos, a navegação mantém a aba atual e troca só os parâmetros.
  private navegar(parametros: Params): void {
    void this.router.navigate([], { relativeTo: this.rotaAtiva, queryParams: parametros });
  }

  private usaPeriodo(): boolean {
    return this.rotaAtiva.firstChild?.snapshot.data['usaPeriodo'] !== false;
  }
}
