import { FormaPagamento, StatusVenda } from './venda.model';

/** Intervalo de datas do relatório, no formato 'aaaa-mm-dd'. */
export interface PeriodoRelatorio {
  inicio: string;
  fim: string;
}

export interface RelatorioMes {
  /** Mês no formato 'aaaa-mm'. */
  mes: string;
  /** Total vendido no mês, com juros (= pago + a vencer + em atraso). */
  valorVendido: number;
  valorPago: number;
  valorAVencer: number;
  valorEmAtraso: number;
  /** Dinheiro que entrou no mês, inclusive parcelas de vendas antigas. */
  valorRecebido: number;
}

export interface PrevisaoRecebimento {
  mes: string;
  valor: number;
  quantidadeParcelas: number;
}

export interface RelatorioVisaoGeral {
  inicio: string;
  fim: string;
  totalVendido: number;
  totalRecebido: number;
  recebidoEsteMes: number;
  quantidadeVendas: number;
  ticketMedio: number;
  totalAReceber: number;
  totalEmAtraso: number;
  quantidadeParcelasEmAtraso: number;
  /** Nulo quando nenhuma parcela venceu ainda. */
  percentualInadimplencia: number | null;
  meses: RelatorioMes[];
  previsaoRecebimento: PrevisaoRecebimento[];
}

export interface ClienteSaldo {
  clienteId: number;
  nome: string;
  valorAVencer: number;
  valorEmAtraso: number;
  totalEmAberto: number;
}

export interface ParcelaEmAtraso {
  parcelaId: number;
  vendaId: number;
  clienteId: number;
  clienteNome: string;
  clienteTelefone: string | null;
  numero: number;
  quantidadeParcelas: number;
  valor: number;
  /** LocalDate 'aaaa-mm-dd' — exibir com timezone 'UTC'. */
  dataVencimento: string;
  diasEmAtraso: number;
}

export interface RelatorioClientes {
  totalClientes: number;
  clientesSemDebito: number;
  clientesDevendo: number;
  clientesNoPrazo: number;
  clientesEmAtraso: number;
  maioresDevedores: ClienteSaldo[];
  parcelasEmAtraso: ParcelaEmAtraso[];
}

export interface RelatorioVenda {
  vendaId: number;
  dataCriacao: string;
  clienteId: number;
  clienteNome: string;
  status: StatusVenda;
  formaPagamento: FormaPagamento;
  quantidadeParcelas: number;
  valorEntrada: number;
  valorTotalComJuros: number;
  valorPago: number;
  valorAVencer: number;
  valorEmAtraso: number;
}
