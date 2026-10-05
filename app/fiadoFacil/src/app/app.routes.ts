import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./shared/layout/layout.component').then((m) => m.LayoutComponent),
    children: [
      {
        path: 'home',
        loadComponent: () =>
          import('./features/home/home.component').then((m) => m.HomeComponent),
      },
      {
        path: 'clientes',
        loadComponent: () =>
          import('./features/clientes/lista/clientes-lista.component').then(
            (m) => m.ClientesListaComponent,
          ),
      },
      {
        path: 'clientes/novo',
        loadComponent: () =>
          import('./features/clientes/formulario/cliente-formulario.component').then(
            (m) => m.ClienteFormularioComponent,
          ),
      },
      {
        path: 'clientes/:id',
        loadComponent: () =>
          import('./features/clientes/perfil/cliente-perfil.component').then(
            (m) => m.ClientePerfilComponent,
          ),
      },
      {
        path: 'clientes/:id/editar',
        loadComponent: () =>
          import('./features/clientes/formulario/cliente-formulario.component').then(
            (m) => m.ClienteFormularioComponent,
          ),
      },
      {
        path: 'clientes/:id/vendas/nova',
        loadComponent: () =>
          import('./features/vendas/nova/nova-venda.component').then((m) => m.NovaVendaComponent),
      },
      {
        path: 'clientes/:id/vendas/nova/finalizar',
        loadComponent: () =>
          import('./features/vendas/finalizar/finalizar-venda.component').then(
            (m) => m.FinalizarVendaComponent,
          ),
      },
      {
        path: 'clientes/:id/vendas/:vendaId/editar',
        loadComponent: () =>
          import('./features/vendas/nova/nova-venda.component').then((m) => m.NovaVendaComponent),
      },
      {
        path: 'clientes/:id/vendas/:vendaId/editar/finalizar',
        loadComponent: () =>
          import('./features/vendas/finalizar/finalizar-venda.component').then(
            (m) => m.FinalizarVendaComponent,
          ),
      },
      {
        path: 'relatorios',
        loadComponent: () =>
          import('./features/relatorios/relatorios.component').then((m) => m.RelatoriosComponent),
        children: [
          {
            path: 'visao-geral',
            loadComponent: () =>
              import('./features/relatorios/visao-geral/visao-geral.component').then(
                (m) => m.VisaoGeralComponent,
              ),
          },
          {
            path: 'clientes',
            // Dívida é situação de hoje: a aba não usa o filtro de período.
            data: { usaPeriodo: false },
            loadComponent: () =>
              import('./features/relatorios/clientes/relatorio-clientes.component').then(
                (m) => m.RelatorioClientesComponent,
              ),
          },
          {
            path: 'historico',
            loadComponent: () =>
              import('./features/relatorios/historico/historico-vendas.component').then(
                (m) => m.HistoricoVendasComponent,
              ),
          },
          { path: '', pathMatch: 'full', redirectTo: 'visao-geral' },
        ],
      },
      {
        path: 'guia',
        loadComponent: () =>
          import('./features/guia/guia.component').then((m) => m.GuiaComponent),
      },
      {
        path: 'personalizacao',
        loadComponent: () =>
          import('./features/personalizacao/personalizacao.component').then(
            (m) => m.PersonalizacaoComponent,
          ),
      },
      { path: '', pathMatch: 'full', redirectTo: 'home' },
    ],
  },
  { path: '**', redirectTo: 'login' },
];
