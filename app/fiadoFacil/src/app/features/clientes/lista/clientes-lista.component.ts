import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, effect, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { Cliente } from '../../../core/models/cliente.model';
import { ClienteService } from '../../../core/services/cliente.service';

type FiltroStatus = 'TODOS' | 'EM_ABERTO' | 'EM_DIA';
type Ordenacao = 'NOME_ASC' | 'NOME_DESC' | 'MAIOR_ABERTO' | 'ULTIMA_COMPRA';

const CLIENTES_POR_PAGINA = 5;

@Component({
  selector: 'app-clientes-lista',
  imports: [CurrencyPipe, DatePipe, FormsModule, RouterLink],
  templateUrl: './clientes-lista.component.html',
  styleUrl: './clientes-lista.component.css',
})
export class ClientesListaComponent {
  private readonly clienteService = inject(ClienteService);
  private readonly rotaAtiva = inject(ActivatedRoute);

  protected readonly clientes = signal<Cliente[]>([]);
  protected readonly carregando = signal(true);
  protected readonly mensagemErro = signal('');

  protected readonly busca = signal('');
  protected readonly filtroStatus = signal<FiltroStatus>('TODOS');
  protected readonly ordenacao = signal<Ordenacao>('NOME_ASC');
  protected readonly pagina = signal(1);

  /** A busca do topo da tela chega por query param e recarrega a lista. */
  private readonly buscaDaRota = toSignal(this.rotaAtiva.queryParamMap, { initialValue: null });

  protected readonly clientesFiltrados = computed(() => {
    const termo = this.busca().trim().toLowerCase();
    const status = this.filtroStatus();

    const filtrados = this.clientes().filter((cliente) => {
      const combinaTermo =
        !termo ||
        cliente.nome.toLowerCase().includes(termo) ||
        (cliente.telefone ?? '').toLowerCase().includes(termo);

      const combinaStatus =
        status === 'TODOS' ||
        (status === 'EM_ABERTO' ? cliente.totalEmAberto > 0 : cliente.totalEmAberto <= 0);

      return combinaTermo && combinaStatus;
    });

    return this.ordenar(filtrados);
  });

  protected readonly totalPaginas = computed(() =>
    Math.max(1, Math.ceil(this.clientesFiltrados().length / CLIENTES_POR_PAGINA)),
  );

  protected readonly clientesDaPagina = computed(() => {
    const inicio = (this.pagina() - 1) * CLIENTES_POR_PAGINA;

    return this.clientesFiltrados().slice(inicio, inicio + CLIENTES_POR_PAGINA);
  });

  constructor() {
    effect(() => {
      this.busca.set(this.buscaDaRota()?.get('busca') ?? '');
    });

    // Ao mudar filtros a paginação volta para a primeira página.
    effect(() => {
      this.clientesFiltrados();
      this.pagina.set(1);
    });

    this.carregar();
  }

  protected carregar(): void {
    this.carregando.set(true);
    this.mensagemErro.set('');

    this.clienteService.listar().subscribe({
      next: (clientes) => {
        this.clientes.set(clientes);
        this.carregando.set(false);
      },
      error: () => {
        this.mensagemErro.set('Não foi possível carregar os clientes.');
        this.carregando.set(false);
      },
    });
  }

  protected irParaPagina(destino: number): void {
    this.pagina.set(Math.min(Math.max(1, destino), this.totalPaginas()));
  }

  protected iniciais(nome: string): string {
    return nome
      .split(' ')
      .filter((parte) => parte.length > 0)
      .slice(0, 2)
      .map((parte) => parte[0].toUpperCase())
      .join('');
  }

  private ordenar(clientes: Cliente[]): Cliente[] {
    const ordenados = [...clientes];

    switch (this.ordenacao()) {
      case 'NOME_DESC':
        return ordenados.sort((a, b) => b.nome.localeCompare(a.nome, 'pt-BR'));
      case 'MAIOR_ABERTO':
        return ordenados.sort((a, b) => b.totalEmAberto - a.totalEmAberto);
      case 'ULTIMA_COMPRA':
        return ordenados.sort((a, b) => (b.ultimaCompra ?? '').localeCompare(a.ultimaCompra ?? ''));
      default:
        return ordenados.sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'));
    }
  }
}
