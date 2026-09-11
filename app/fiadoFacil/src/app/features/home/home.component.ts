import { CurrencyPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Painel } from '../../core/models/painel.model';
import { AuthService } from '../../core/services/auth.service';
import { PainelService } from '../../core/services/painel.service';

@Component({
  selector: 'app-home',
  imports: [CurrencyPipe, RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
})
export class HomeComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly painelService = inject(PainelService);

  protected readonly painel = signal<Painel | null>(null);
  protected readonly carregando = signal(true);
  protected readonly mensagemErro = signal('');

  protected readonly primeiroNome = this.authService.getUsuarioLogado()?.nomeEmpresa.split(' ')[0] ?? '';

  ngOnInit(): void {
    this.carregarResumo();
  }

  protected carregarResumo(): void {
    this.carregando.set(true);
    this.mensagemErro.set('');

    this.painelService.buscarResumo().subscribe({
      next: (resumo) => {
        this.painel.set(resumo);
        this.carregando.set(false);
      },
      error: () => {
        this.mensagemErro.set('Não foi possível carregar os dados do painel.');
        this.carregando.set(false);
      },
    });
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
