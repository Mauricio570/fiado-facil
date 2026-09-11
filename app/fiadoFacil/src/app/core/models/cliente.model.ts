export interface ClienteRequest {
  nome: string;
  telefone: string | null;
  cpf: string | null;
  endereco: string | null;
}

export interface Cliente {
  id: number;
  nome: string;
  telefone: string | null;
  cpf: string | null;
  endereco: string | null;
  dataCriacao: string;
  totalEmAberto: number;
  ultimaCompra: string | null;
}
