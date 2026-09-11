export interface UsuarioLogado {
  id: number;
  email: string;
  nomeEmpresa: string;
}

export interface Usuario {
  id: number;
  nomeEmpresa: string;
  cnpj: string;
  email: string;
  dataCriacao: string;
}

export interface UsuarioAtualizacaoRequest {
  nomeEmpresa: string;
  cnpj: string;
  email: string;
  senhaAtual: string | null;
  novaSenha: string | null;
  repetirNovaSenha: string | null;
}
