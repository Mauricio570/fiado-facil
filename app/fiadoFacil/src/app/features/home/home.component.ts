import { CurrencyPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { Router } from '@angular/router';

import { Painel } from '../../core/models/painel.model';
import { AuthService } from '../../core/services/auth.service';
import { PainelService } from '../../core/services/painel.service';

@Component({
  selector: 'app-home',
  imports: [CurrencyPipe],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
})
export class HomeComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly painelService = inject(PainelService);
  private readonly router = inject(Router);

  protected readonly painel = signal<Painel | null>(null);
  protected readonly carregando = signal(true);
  protected readonly mensagemErro = signal('');

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

  protected sair(): void {
    this.authService.logout();
    void this.router.navigate(['/login']);
  }
}
