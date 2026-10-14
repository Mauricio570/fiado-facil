import { FormaPagamento, StatusVenda } from './venda.model';

export interface PeriodoRelatorio {
  inicio: string;
  fim: string;
}

export interface RelatorioMes {
  mes: string;
  valorVendido: number;
  valorPago: number;
  valorAVencer: number;
  valorEmAtraso: number;
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
