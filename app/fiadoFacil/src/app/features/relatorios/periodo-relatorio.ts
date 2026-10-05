import { ParamMap } from '@angular/router';

import { PeriodoRelatorio } from '../../core/models/relatorio.model';

export const ATALHOS_DE_MESES = [3, 6, 12] as const;

export type AtalhoDeMeses = (typeof ATALHOS_DE_MESES)[number];

const ATALHO_PADRAO: AtalhoDeMeses = 6;

const NOMES_DOS_MESES = ['jan', 'fev', 'mar', 'abr', 'mai', 'jun', 'jul', 'ago', 'set', 'out', 'nov', 'dez'];

/**
 * Período escolhido na tela. Ele fica na URL — `?meses=3` para os atalhos ou
 * `?inicio=aaaa-mm-dd&fim=aaaa-mm-dd` para um intervalo livre — para continuar
 * valendo ao trocar de aba, ao recarregar a página e no botão voltar.
 */
export interface FiltroPeriodo extends PeriodoRelatorio {
  /** Nulo quando o período é um intervalo livre. */
  meses: AtalhoDeMeses | null;
}

export function lerFiltroPeriodo(parametros: ParamMap): FiltroPeriodo {
  const inicio = parametros.get('inicio');
  const fim = parametros.get('fim');

  if (ehDataValida(inicio) && ehDataValida(fim)) {
    return { meses: null, inicio, fim };
  }

  const meses =
    ATALHOS_DE_MESES.find((atalho) => atalho === Number(parametros.get('meses'))) ?? ATALHO_PADRAO;

  return { meses, ...ultimosMeses(meses) };
}

/** O mês atual e os anteriores: do dia 1 do primeiro mês até hoje. */
export function ultimosMeses(meses: number): PeriodoRelatorio {
  const hoje = new Date();
  const inicio = new Date(hoje.getFullYear(), hoje.getMonth() - (meses - 1), 1);

  return { inicio: dataIso(inicio), fim: dataIso(hoje) };
}

export function mesmoPeriodo(a: PeriodoRelatorio, b: PeriodoRelatorio): boolean {
  return a.inicio === b.inicio && a.fim === b.fim;
}

/** '2026-07' vira 'jul/26', rótulo curto para o eixo dos gráficos. */
export function rotuloDoMes(mes: string): string {
  const [ano, numero] = mes.split('-');

  return `${NOMES_DOS_MESES[Number(numero) - 1]}/${ano.slice(2)}`;
}

/**
 * Data local no formato 'aaaa-mm-dd'. O toISOString() converteria para UTC
 * e, à noite no Brasil, já devolveria o dia seguinte.
 */
function dataIso(data: Date): string {
  const mes = String(data.getMonth() + 1).padStart(2, '0');
  const dia = String(data.getDate()).padStart(2, '0');

  return `${data.getFullYear()}-${mes}-${dia}`;
}

// Uma data digitada à mão na URL que não seja 'aaaa-mm-dd' volta para o
// período padrão, em vez de quebrar a tela.
function ehDataValida(valor: string | null): valor is string {
  return !!valor && /^\d{4}-\d{2}-\d{2}$/.test(valor) && !Number.isNaN(Date.parse(valor));
}
