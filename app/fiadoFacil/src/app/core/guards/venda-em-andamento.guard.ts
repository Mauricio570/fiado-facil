import { inject } from '@angular/core';
import {
  CanDeactivateFn,
  NavigationCancel,
  NavigationEnd,
  NavigationError,
  Router,
} from '@angular/router';
import { filter, take } from 'rxjs';

import { AuthService } from '../services/auth.service';
import { NovaVendaStore } from '../services/nova-venda.store';

export interface TelaDeVenda {
  confirmarSaida(): Promise<boolean>;
}

export const vendaEmAndamentoGuard: CanDeactivateFn<TelaDeVenda> = async (
  tela,
  _rotaAtual,
  _estadoAtual,
  proximoEstado,
) => {
  const store = inject(NovaVendaStore);
  const authService = inject(AuthService);
  const router = inject(Router);

  if (proximoEstado.url.startsWith(`/clientes/${store.clienteId()}/vendas/`)) {
    return true;
  }

  const podeSair =
    store.itens().length === 0 || !authService.estaAutenticado() || (await tela.confirmarSaida());

  if (podeSair) {
    router.events
      .pipe(
        filter(
          (evento) =>
            evento instanceof NavigationEnd ||
            evento instanceof NavigationCancel ||
            evento instanceof NavigationError,
        ),
        take(1),
      )
      .subscribe((evento) => {
        if (evento instanceof NavigationEnd) {
          store.limpar();
        }
      });
  }

  return podeSair;
};
