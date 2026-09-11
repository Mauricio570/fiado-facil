/**
 * A API guarda o endereço em um único campo `endereco` (String, máx. 255),
 * mas a tela de cadastro trabalha com os campos separados do layout.
 * Estas funções fazem a ponte entre os dois formatos.
 *
 * Formato gerado (partes vazias são omitidas):
 *   `logradouro, numero, complemento - bairro - cidade/UF - CEP 00000-000`
 */
export interface EnderecoDetalhado {
  cep: string;
  logradouro: string;
  numero: string;
  complemento: string;
  bairro: string;
  cidade: string;
  estado: string;
}

export const ENDERECO_VAZIO: EnderecoDetalhado = {
  cep: '',
  logradouro: '',
  numero: '',
  complemento: '',
  bairro: '',
  cidade: '',
  estado: '',
};

const SEPARADOR_CEP = ' - CEP ';

export function montarEndereco(endereco: EnderecoDetalhado): string | null {
  const logradouroCompleto = [endereco.logradouro, endereco.numero, endereco.complemento]
    .map((parte) => parte.trim())
    .filter((parte) => parte.length > 0)
    .join(', ');

  const localidade = [endereco.cidade, endereco.estado]
    .map((parte) => parte.trim())
    .filter((parte) => parte.length > 0)
    .join('/');

  const partes = [logradouroCompleto, endereco.bairro.trim(), localidade].filter(
    (parte) => parte.length > 0,
  );

  const cep = endereco.cep.trim();
  const texto = partes.join(' - ') + (cep ? `${partes.length ? SEPARADOR_CEP : `CEP `}${cep}` : '');

  return texto.length > 0 ? texto : null;
}

export function separarEndereco(endereco: string | null): EnderecoDetalhado {
  if (!endereco?.trim()) {
    return { ...ENDERECO_VAZIO };
  }

  const [semCep, cep = ''] = endereco.split(SEPARADOR_CEP);
  const blocos = semCep.split(' - ').map((bloco) => bloco.trim());

  // O último bloco só é cidade/UF quando existe a barra separando os dois.
  const blocoLocalidade = blocos.length > 1 && blocos[blocos.length - 1].includes('/')
    ? blocos.pop()!
    : '';
  const [cidade = '', estado = ''] = blocoLocalidade.split('/').map((parte) => parte.trim());

  const bairro = blocos.length > 1 ? blocos.pop()! : '';
  const [logradouro = '', numero = '', complemento = ''] = (blocos[0] ?? '')
    .split(',')
    .map((parte) => parte.trim());

  return { cep: cep.trim(), logradouro, numero, complemento, bairro, cidade, estado };
}
