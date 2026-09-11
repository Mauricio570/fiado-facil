import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin, finalize } from 'rxjs';

import { Cliente } from '../../../core/models/cliente.model';
import { Parcela, StatusVenda, Venda, VendaResumo } from '../../../core/models/venda.model';
import { ClienteService } from '../../../core/services/cliente.service';
import { VendaService } from '../../../core/services/venda.service';
import { mensagemDoErro } from '../../../shared/utils/erro.util';

type FiltroStatus = 'TODOS' | StatusVenda;

const VENDAS_POR_PAGINA = 5;

@Component({
  selector: 'app-cliente-perfil',
  imports: [CurrencyPipe, DatePipe, FormsModule, RouterLink],
  templateUrl: './cliente-perfil.component.html',
  styleUrl: './cliente-perfil.component.css',
})
export class ClientePerfilComponent implements OnInit {
  private readonly clienteService = inject(ClienteService);
  private readonly vendaService = inject(VendaService);
  private readonly rotaAtiva = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly cliente = signal<Cliente | null>(null);
  protected readonly vendas = signal<VendaResumo[]>([]);
  protected readonly carregando = signal(true);
  protected readonly mensagemErro = signal('');

  protected readonly filtroStatus = signal<FiltroStatus>('TODOS');
  protected readonly pagina = signal(1);
  protected readonly menuAberto = signal(false);

  protected readonly vendaExpandida = signal<number | null>(null);
  protected readonly detalheVenda = signal<Venda | null>(null);
  protected readonly carregandoDetalhe = signal(false);

  /** Parcela aguardando confirmação no modal — evita marcar como paga por engano. */
  protected readonly parcelaParaConfirmar = signal<Parcela | null>(null);
  protected readonly confirmandoPagamento = signal(false);

  /** Exclusão do cliente também passa por modal próprio, nunca pelo confirm() do navegador. */
  protected readonly confirmandoExclusao = signal(false);
  protected readonly excluindo = signal(false);

  /** Venda aguardando confirmação de exclusão no modal. */
  protected readonly vendaParaExcluir = signal<VendaResumo | null>(null);
  protected readonly excluindoVenda = signal(false);

  protected readonly vendasFiltradas = computed(() => {
    const status = this.filtroStatus();

    return this.vendas().filter((venda) => status === 'TODOS' || venda.status === status);
  });

  /** Números do cabeçalho do histórico, calculados sobre as vendas já carregadas. */
  protected readonly resumoHistorico = computed(() => {
    const vendas = this.vendas();
    const emAberto = vendas.filter((venda) => venda.status === 'EM_ABERTO');

    return {
      total: vendas.length,
      emAberto: emAberto.length,
      pagas: vendas.length - emAberto.length,
    };
  });

  /** Progresso das parcelas da venda aberta, para a barra do painel de detalhe. */
  protected readonly progressoParcelas = computed(() => {
    const parcelas = this.detalheVenda()?.pagamento.parcelas ?? [];
    const pagas = parcelas.filter((parcela) => parcela.status === 'PAGO').length;

    return {
      pagas,
      total: parcelas.length,
      percentual: parcelas.length === 0 ? 0 : Math.round((pagas / parcelas.length) * 100),
    };
  });

  protected readonly totalPaginas = computed(() =>
    Math.max(1, Math.ceil(this.vendasFiltradas().length / VENDAS_POR_PAGINA)),
  );

  protected readonly vendasDaPagina = computed(() => {
    const inicio = (this.pagina() - 1) * VENDAS_POR_PAGINA;

    return this.vendasFiltradas().slice(inicio, inicio + VENDAS_POR_PAGINA);
  });

  private clienteId = 0;

  ngOnInit(): void {
    this.clienteId = Number(this.rotaAtiva.snapshot.paramMap.get('id'));
    this.carregar();
  }

  protected carregar(): void {
    this.carregando.set(true);
    this.mensagemErro.set('');

    forkJoin({
      cliente: this.clienteService.buscarPorId(this.clienteId),
      vendas: this.vendaService.listarPorCliente(this.clienteId),
    })
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: ({ cliente, vendas }) => {
          this.cliente.set(cliente);
          this.vendas.set(vendas);
        },
        error: () => this.mensagemErro.set('Não foi possível carregar os dados do cliente.'),
      });
  }

  protected alternarDetalhe(venda: VendaResumo): void {
    if (this.vendaExpandida() === venda.id) {
      this.vendaExpandida.set(null);
      this.detalheVenda.set(null);
      return;
    }

    this.vendaExpandida.set(venda.id);
    this.detalheVenda.set(null);
    this.carregandoDetalhe.set(true);

    this.vendaService
      .buscarDetalhe(venda.id)
      .pipe(finalize(() => this.carregandoDetalhe.set(false)))
      .subscribe({
        next: (detalhe) => this.detalheVenda.set(detalhe),
        error: () => this.mensagemErro.set('Não foi possível carregar os detalhes da venda.'),
      });
  }

  protected pedirConfirmacaoDePagamento(parcela: Parcela): void {
    this.parcelaParaConfirmar.set(parcela);
  }

  protected cancelarPagamento(): void {
    this.parcelaParaConfirmar.set(null);
  }

  protected confirmarPagamento(): void {
    const parcela = this.parcelaParaConfirmar();

    if (!parcela) {
      return;
    }

    this.confirmandoPagamento.set(true);

    this.vendaService
      .marcarParcelaComoPaga(parcela.id)
      .pipe(finalize(() => this.confirmandoPagamento.set(false)))
      .subscribe({
        next: () => {
          this.parcelaParaConfirmar.set(null);

          const vendaAberta = this.vendaExpandida();
          this.carregar();

          if (vendaAberta) {
            this.vendaService.buscarDetalhe(vendaAberta).subscribe({
              next: (detalhe) => this.detalheVenda.set(detalhe),
            });
          }
        },
        error: (erro) => {
          this.parcelaParaConfirmar.set(null);
          this.mensagemErro.set(
            mensagemDoErro(erro, 'Não foi possível registrar o pagamento da parcela.'),
          );
        },
      });
  }

  protected pedirConfirmacaoDeExclusao(): void {
    this.menuAberto.set(false);
    this.confirmandoExclusao.set(true);
  }

  protected cancelarExclusao(): void {
    this.confirmandoExclusao.set(false);
  }

  protected confirmarExclusao(): void {
    this.excluindo.set(true);

    this.clienteService
      .excluir(this.clienteId)
      .pipe(finalize(() => this.excluindo.set(false)))
      .subscribe({
        next: () => {
          this.confirmandoExclusao.set(false);
          void this.router.navigate(['/clientes']);
        },
        error: (erro) => {
          this.confirmandoExclusao.set(false);
          this.mensagemErro.set(mensagemDoErro(erro, 'Não foi possível excluir o cliente.'));
        },
      });
  }

  protected editarVenda(venda: VendaResumo): void {
    void this.router.navigate(['/clientes', this.clienteId, 'vendas', venda.id, 'editar']);
  }

  protected pedirConfirmacaoDeExclusaoDeVenda(venda: VendaResumo): void {
    this.vendaParaExcluir.set(venda);
  }

  protected cancelarExclusaoDeVenda(): void {
    this.vendaParaExcluir.set(null);
  }

  protected confirmarExclusaoDeVenda(): void {
    const venda = this.vendaParaExcluir();

    if (!venda) {
      return;
    }

    this.excluindoVenda.set(true);

    this.vendaService
      .excluir(venda.id)
      .pipe(finalize(() => this.excluindoVenda.set(false)))
      .subscribe({
        next: () => {
          this.vendaParaExcluir.set(null);

          if (this.vendaExpandida() === venda.id) {
            this.vendaExpandida.set(null);
            this.detalheVenda.set(null);
          }

          this.carregar();
        },
        error: (erro) => {
          this.vendaParaExcluir.set(null);
          this.mensagemErro.set(mensagemDoErro(erro, 'Não foi possível excluir a compra.'));
        },
      });
  }

  /** Texto do tooltip explicando por que os botões estão travados. */
  protected motivoBloqueio(venda: VendaResumo): string {
    if (venda.podeAlterar) {
      return '';
    }

    return venda.status === 'PAGO'
      ? 'Esta compra já foi quitada e não pode mais ser alterada ou excluída.'
      : 'Esta compra já teve pagamento registrado e não pode mais ser alterada ou excluída.';
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
}
