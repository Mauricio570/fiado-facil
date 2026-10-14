export type TipoMascara = 'telefone' | 'cpf' | 'cnpj' | 'cep';

export const TAMANHO_MAXIMO: Record<TipoMascara, number> = {
  telefone: 15,
  cpf: 14,
  cnpj: 18,
  cep: 9,
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
