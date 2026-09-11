/**
 * Formatação dos campos que o usuário digita apenas com números.
 * Cada função ignora o que não for dígito, corta no limite do formato e
 * acrescenta a pontuação conforme o texto cresce — assim o campo já vai
 * ficando formatado enquanto o usuário digita.
 */
export type TipoMascara = 'telefone' | 'cpf' | 'cnpj' | 'cep';

/** Quantidade máxima de caracteres do campo já formatado. */
export const TAMANHO_MAXIMO: Record<TipoMascara, number> = {
  telefone: 15, // (51) 99932-3245
  cpf: 14, // 123.456.789-01
  cnpj: 18, // 12.345.678/0001-90
  cep: 9, // 90000-000
};

export function aplicarMascara(valor: string, tipo: TipoMascara): string {
  switch (tipo) {
    case 'telefone':
      return formatarTelefone(valor);
    case 'cpf':
      return formatarCpf(valor);
    case 'cnpj':
      return formatarCnpj(valor);
    case 'cep':
      return formatarCep(valor);
  }
}

function somenteDigitos(valor: string, maximo: number): string {
  return valor.replace(/\D/g, '').slice(0, maximo);
}

/** Aceita fixo com 10 dígitos — (51) 3232-1010 — e celular com 11. */
function formatarTelefone(valor: string): string {
  const digitos = somenteDigitos(valor, 11);

  if (digitos.length <= 2) {
    return digitos;
  }

  const ddd = `(${digitos.slice(0, 2)}) `;

  if (digitos.length <= 6) {
    return ddd + digitos.slice(2);
  }

  const corte = digitos.length <= 10 ? 6 : 7;

  return `${ddd}${digitos.slice(2, corte)}-${digitos.slice(corte)}`;
}

function formatarCpf(valor: string): string {
  const digitos = somenteDigitos(valor, 11);

  if (digitos.length <= 3) {
    return digitos;
  }

  if (digitos.length <= 6) {
    return `${digitos.slice(0, 3)}.${digitos.slice(3)}`;
  }

  if (digitos.length <= 9) {
    return `${digitos.slice(0, 3)}.${digitos.slice(3, 6)}.${digitos.slice(6)}`;
  }

  return `${digitos.slice(0, 3)}.${digitos.slice(3, 6)}.${digitos.slice(6, 9)}-${digitos.slice(9)}`;
}

function formatarCnpj(valor: string): string {
  const digitos = somenteDigitos(valor, 14);

  if (digitos.length <= 2) {
    return digitos;
  }

  if (digitos.length <= 5) {
    return `${digitos.slice(0, 2)}.${digitos.slice(2)}`;
  }

  if (digitos.length <= 8) {
    return `${digitos.slice(0, 2)}.${digitos.slice(2, 5)}.${digitos.slice(5)}`;
  }

  if (digitos.length <= 12) {
    return `${digitos.slice(0, 2)}.${digitos.slice(2, 5)}.${digitos.slice(5, 8)}/${digitos.slice(8)}`;
  }

  return (
    `${digitos.slice(0, 2)}.${digitos.slice(2, 5)}.${digitos.slice(5, 8)}` +
    `/${digitos.slice(8, 12)}-${digitos.slice(12)}`
  );
}

function formatarCep(valor: string): string {
  const digitos = somenteDigitos(valor, 8);

  return digitos.length <= 5 ? digitos : `${digitos.slice(0, 5)}-${digitos.slice(5)}`;
}
