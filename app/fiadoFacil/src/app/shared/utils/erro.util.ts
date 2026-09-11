import { HttpErrorResponse } from '@angular/common/http';

/**
 * Corpo de erro padronizado pela API (ErroResponse). A mensagem já vem pronta
 * para ser exibida ao usuário, então a tela apenas repassa o texto.
 */
interface ErroDaApi {
  status: number;
  mensagem: string;
  campos?: Record<string, string>;
}

/**
 * Extrai a mensagem que a API mandou. O texto alternativo só é usado quando a
 * resposta não veio no formato esperado — por exemplo, se a API estiver fora
 * do ar e o erro for de rede.
 */
export function mensagemDoErro(erro: unknown, alternativa: string): string {
  const corpo = erro instanceof HttpErrorResponse ? (erro.error as ErroDaApi | null) : null;

  return corpo?.mensagem?.trim() || alternativa;
}

/** Erros por campo devolvidos pela validação da API, quando houver. */
export function camposComErro(erro: unknown): Record<string, string> {
  const corpo = erro instanceof HttpErrorResponse ? (erro.error as ErroDaApi | null) : null;

  return corpo?.campos ?? {};
}
