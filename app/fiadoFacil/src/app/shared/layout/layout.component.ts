import { DOCUMENT } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';

import { AuthService } from '../../core/services/auth.service';
import { PreferenciaService } from '../../core/services/preferencia.service';

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

  protected readonly menuExpandido = signal(!this.ehTelaEstreita());
  protected readonly usuario = this.authService.getUsuarioLogado();

  ngOnInit(): void {
    this.preferenciaService.carregar().subscribe({ error: () => undefined });

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

  protected sair(): void {
    void this.router.navigate(['/login']).then((saiu) => {
      if (saiu) {
        this.authService.logout();
        this.preferenciaService.limpar();
      }
    });
  }

  private ehTelaEstreita(): boolean {
    return (this.documento.defaultView?.innerWidth ?? LARGURA_DESKTOP + 1) <= LARGURA_DESKTOP;
  }
}
