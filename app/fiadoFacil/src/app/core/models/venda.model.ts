export type FormaPagamento = 'A_VISTA' | 'CREDITO';

export type StatusVenda = 'EM_ABERTO' | 'PAGO';

export type StatusParcela = 'EM_ABERTO' | 'PAGO';

export interface ItemVendaRequest {
  nome: string;
  quantidade: number;
  valorUnitario: number;
}

export interface PagamentoRequest {
  formaPagamento: FormaPagamento;
  quantidadeParcelas: number | null;
  jurosMes: number | null;
  valorEntrada: number | null;
}

export interface VendaRequest {
  fkCliente: number;
  itens: ItemVendaRequest[];
  pagamento: PagamentoRequest;
}

export interface ItemVenda {
  id: number;
  nome: string;
  quantidade: number;
  valorUnitario: number;
  valorTotal: number;
}

export interface Parcela {
  id: number;
  numero: number;
  valor: number;
  /** Data prevista de pagamento (LocalDate 'aaaa-mm-dd'). É só previsão. */
  dataVencimento: string;
  dataPagamento: string | null;
  status: StatusParcela;
}

export interface Pagamento {
  id: number;
  formaPagamento: FormaPagamento;
  quantidadeParcelas: number;
  jurosMes: number;
  valorEntrada: number;
  parcelas: Parcela[];
}

export interface Venda {
  id: number;
  clienteId: number;
  clienteNome: string;
  status: StatusVenda;
  dataCriacao: string;
  valorTotal: number;
  itens: ItemVenda[];
  pagamento: Pagamento;
}

export interface VendaResumo {
  id: number;
  status: StatusVenda;
  dataCriacao: string;
  /** Soma dos produtos, sem juros. */
  valorTotal: number;
  /** Total que o cliente paga pela venda, já com juros. */
  valorTotalComJuros: number;
  totalEmAberto: number;
  /** Falso assim que qualquer pagamento é registrado (entrada, parcela paga ou venda quitada). */
  podeAlterar: boolean;
}
