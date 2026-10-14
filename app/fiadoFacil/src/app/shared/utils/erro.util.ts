import { HttpErrorResponse } from '@angular/common/http';

interface ErroDaApi {
  status: number;
  mensagem: string;
  campos?: Record<string, string>;
}

export function mensagemDoErro(erro: unknown, alternativa: string): string {
  const corpo = erro instanceof HttpErrorResponse ? (erro.error as ErroDaApi | null) : null;

  return corpo?.mensagem?.trim() || alternativa;
}

export function camposComErro(erro: unknown): Record<string, string> {
  const corpo = erro instanceof HttpErrorResponse ? (erro.error as ErroDaApi | null) : null;

  return corpo?.campos ?? {};
}
