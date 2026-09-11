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
