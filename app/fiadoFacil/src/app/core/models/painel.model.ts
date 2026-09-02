export interface ClienteEmDebito {
  clienteId: number;
  nome: string;
  totalEmAberto: number;
}

export interface Painel {
  totalAReceber: number;
  recebidoEsteMes: number;
  clientesEmDebito: number;
  totalClientes: number;
  clientesComContasEmAberto: ClienteEmDebito[];
}
