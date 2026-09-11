import { DOCUMENT } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';

import { AuthService } from '../../core/services/auth.service';
import { PreferenciaService } from '../../core/services/preferencia.service';

/** Acima disso a barra lateral é fixa; abaixo, vira gaveta sobreposta. */
const LARGURA_DESKTOP = 760;

@Component({
  selector: 'app-layout',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.css',
})
export class LayoutComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly documento = inject(DOCUMENT);
  private readonly preferenciaService = inject(PreferenciaService);

  /**
   * No desktop indica se o menu está expandido ou recolhido em ícones; no
   * celular indica se a gaveta está aberta. Começa fechado no celular para o
   * conteúdo ocupar a largura inteira.
   */
  protected readonly menuExpandido = signal(!this.ehTelaEstreita());
  protected readonly usuario = this.authService.getUsuarioLogado();

  ngOnInit(): void {
    // Sincroniza a escala de texto com o que está salvo na conta. Se falhar,
    // segue valendo o que já foi aplicado a partir do navegador.
    this.preferenciaService.carregar().subscribe({ error: () => undefined });

    // No celular a gaveta cobre a tela, então precisa fechar ao navegar.
    this.router.events
      .pipe(filter((evento) => evento instanceof NavigationEnd))
      .subscribe(() => this.fecharSeEstreita());
  }

  protected alternarMenu(): void {
    this.menuExpandido.update((expandido) => !expandido);
  }

  protected fecharSeEstreita(): void {
    if (this.ehTelaEstreita()) {
      this.menuExpandido.set(false);
    }
  }

  protected buscarCliente(termo: string): void {
    void this.router.navigate(['/clientes'], {
      queryParams: { busca: termo.trim() || null },
    });
  }

  protected sair(): void {
    this.authService.logout();
    this.preferenciaService.limpar();
    void this.router.navigate(['/login']);
  }

  private ehTelaEstreita(): boolean {
    return (this.documento.defaultView?.innerWidth ?? LARGURA_DESKTOP + 1) <= LARGURA_DESKTOP;
  }
}
