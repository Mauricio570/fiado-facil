import {
  Component,
  DestroyRef,
  ElementRef,
  computed,
  effect,
  inject,
  input,
  signal,
  untracked,
  viewChild,
} from '@angular/core';
import {
  ArcElement,
  BarController,
  BarElement,
  CategoryScale,
  Chart,
  ChartConfiguration,
  DoughnutController,
  Filler,
  LineController,
  LineElement,
  LinearScale,
  Plugin,
  PointElement,
  Tooltip,
  TooltipItem,
} from 'chart.js';

// Registra só as peças do Chart.js usadas aqui, para o bundle não levar a biblioteca inteira.
Chart.register(
  ArcElement,
  BarController,
  BarElement,
  CategoryScale,
  DoughnutController,
  Filler,
  LineController,
  LineElement,
  LinearScale,
  PointElement,
  Tooltip,
);

export type TipoGrafico = 'colunas' | 'barras' | 'area' | 'rosca';

export interface SerieGrafico {
  nome: string;
  valores: number[];
  /** Uma cor para a série toda, ou uma por ponto (rosca e colunas com destaque). */
  cor: string | string[];
}

export interface ItemLegenda {
  nome: string;
  cor: string;
  /** Valor exibido ao lado do nome, usado na rosca. */
  valor?: string;
}

export interface DestaqueCentral {
  valor: string;
  rotulo: string;
}

const FONTE = "Inter, ui-sans-serif, system-ui, -apple-system, 'Segoe UI', sans-serif";
const COR_TEXTO_SUAVE = '#5d6564';
const COR_GRADE = '#eef0f1';
const COR_EIXO = '#cfd3d4';
const COR_SUPERFICIE = '#ffffff';

const formatoMoeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
const formatoMoedaCompacta = new Intl.NumberFormat('pt-BR', {
  style: 'currency',
  currency: 'BRL',
  notation: 'compact',
  maximumFractionDigits: 1,
});
const formatoInteiro = new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 0 });

/** Linha vertical fina que acompanha o ponteiro no gráfico de área, marcando o mês lido. */
const linhaGuia: Plugin<'line'> = {
  id: 'linhaGuia',
  beforeDatasetsDraw(grafico) {
    const ativo = grafico.tooltip?.getActiveElements()[0];
    if (!ativo) {
      return;
    }

    const { ctx, chartArea } = grafico;
    ctx.save();
    ctx.strokeStyle = COR_EIXO;
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.moveTo(ativo.element.x, chartArea.top);
    ctx.lineTo(ativo.element.x, chartArea.bottom);
    ctx.stroke();
    ctx.restore();
  },
};

/**
 * Gráfico dos relatórios sobre o Chart.js, com o visual do SafeBook já
 * aplicado: barras finas com canto arredondado só na ponta, fresta branca
 * entre segmentos empilhados, grade discreta, valores em reais e tooltip.
 *
 * A legenda é HTML (e não desenhada no canvas) para seguir a tipografia do
 * sistema, e todo gráfico tem uma visão em tabela com os mesmos números —
 * o canvas sozinho não é lido por leitores de tela.
 */
@Component({
  selector: 'app-grafico',
  templateUrl: './grafico.component.html',
  styleUrl: './grafico.component.css',
})
export class GraficoComponent {
  readonly tipo = input.required<TipoGrafico>();
  readonly rotulos = input.required<string[]>();
  readonly series = input.required<SerieGrafico[]>();
  /** Frase que descreve o gráfico para leitores de tela. */
  readonly descricao = input.required<string>();
  /** Cabeçalho da primeira coluna na visão em tabela ("Mês", "Cliente"...). */
  readonly rotuloEixo = input('');
  readonly empilhado = input(false);
  readonly formato = input<'moeda' | 'inteiro'>('moeda');
  /** Singular e plural da unidade dos valores inteiros, como ['cliente', 'clientes']. */
  readonly unidade = input<[string, string]>(['', '']);
  /** Legenda própria, para quando as cores não seguem uma por série. */
  readonly legenda = input<ItemLegenda[] | null>(null);
  /** Número no centro da rosca. */
  readonly destaque = input<DestaqueCentral | null>(null);
  readonly mensagemVazio = input('Nada para mostrar neste período.');
  readonly altura = input(280);

  protected readonly modoTabela = signal(false);

  protected readonly vazio = computed(() =>
    this.series().every((serie) => serie.valores.every((valor) => !valor)),
  );

  protected readonly itensLegenda = computed<ItemLegenda[]>(() => {
    const propria = this.legenda();
    if (propria) {
      return propria;
    }

    const series = this.series();

    if (this.tipo() === 'rosca') {
      return this.rotulos().map((rotulo, indice) => ({
        nome: rotulo,
        cor: this.corDoPonto(0, indice),
        valor: this.formatar(series[0]?.valores[indice] ?? 0),
      }));
    }

    // Uma série só não precisa de legenda: o título do cartão já diz o que é.
    return series.length < 2
      ? []
      : series.map((serie, indice) => ({ nome: serie.nome, cor: this.corDoPonto(indice, 0) }));
  });

  /** Nas colunas empilhadas as séries são partes de um todo, então a tabela ganha o total. */
  protected readonly linhasTabela = computed(() =>
    this.rotulos().map((rotulo, indice) => {
      const valores = this.series().map((serie) => serie.valores[indice] ?? 0);
      const total = valores.reduce((soma, valor) => soma + valor, 0);

      return {
        rotulo,
        valores: valores.map((valor) => this.formatar(valor)),
        total: this.formatar(total),
      };
    }),
  );

  private readonly canvas = viewChild<ElementRef<HTMLCanvasElement>>('canvas');
  private grafico: Chart | null = null;
  /** Tipo do gráfico desenhado no canvas atual; trocar de tipo exige recriar o gráfico. */
  private tipoDesenhado: TipoGrafico | null = null;

  constructor() {
    // O canvas só existe com dados e fora do modo tabela; quando some, o
    // gráfico é destruído, e quando os dados mudam ele é atualizado no lugar.
    effect(() => {
      const canvas = this.canvas()?.nativeElement;
      const tipo = this.tipo();
      const configuracao = this.montarConfiguracao();

      untracked(() => this.desenhar(canvas, tipo, configuracao));
    });

    inject(DestroyRef).onDestroy(() => this.grafico?.destroy());
  }

  protected formatar(valor: number): string {
    if (this.formato() === 'moeda') {
      return formatoMoeda.format(valor);
    }

    const [singular, plural] = this.unidade();

    return `${formatoInteiro.format(valor)} ${valor === 1 ? singular : plural}`.trim();
  }

  private formatarCompacto(valor: number): string {
    return this.formato() === 'moeda'
      ? formatoMoedaCompacta.format(valor)
      : formatoInteiro.format(valor);
  }

  private corDoPonto(indiceSerie: number, indicePonto: number): string {
    const cor = this.series()[indiceSerie]?.cor ?? COR_EIXO;

    return Array.isArray(cor) ? (cor[indicePonto] ?? cor[0]) : cor;
  }

  private desenhar(
    canvas: HTMLCanvasElement | undefined,
    tipo: TipoGrafico,
    configuracao: ChartConfiguration,
  ): void {
    if (!canvas) {
      this.grafico?.destroy();
      this.grafico = null;
      return;
    }

    if (this.grafico?.canvas === canvas && this.tipoDesenhado === tipo) {
      this.grafico.data = configuracao.data;
      this.grafico.options = configuracao.options ?? {};
      this.grafico.update();
      return;
    }

    this.grafico?.destroy();
    this.grafico = new Chart(canvas, configuracao);
    this.tipoDesenhado = tipo;
  }

  private montarConfiguracao(): ChartConfiguration {
    switch (this.tipo()) {
      case 'rosca':
        return this.configuracaoRosca() as ChartConfiguration;
      case 'area':
        return this.configuracaoArea() as ChartConfiguration;
      case 'barras':
        return this.configuracaoBarras(true) as ChartConfiguration;
      default:
        return this.configuracaoBarras(false) as ChartConfiguration;
    }
  }

  private configuracaoBarras(horizontal: boolean): ChartConfiguration<'bar'> {
    const empilhado = this.empilhado();

    return {
      type: 'bar',
      data: {
        labels: this.rotulos(),
        datasets: this.series().map((serie) => ({
          label: serie.nome,
          data: serie.valores,
          backgroundColor: serie.cor,
          borderColor: COR_SUPERFICIE,
          // Fresta de 2px na cor do fundo entre os segmentos empilhados.
          borderWidth: empilhado ? (horizontal ? { right: 2 } : { top: 2 }) : 0,
          // Com números, o Chart.js arredonda só a ponta da pilha; a base fica reta.
          borderRadius: 4,
          borderSkipped: 'start',
          maxBarThickness: 24,
        })),
      },
      options: {
        ...this.opcoesBase(),
        indexAxis: horizontal ? 'y' : 'x',
        interaction: { mode: 'index', intersect: false, axis: horizontal ? 'y' : 'x' },
        scales: horizontal
          ? { x: this.eixoDeValores(empilhado), y: this.eixoDeCategorias(empilhado, true) }
          : { x: this.eixoDeCategorias(empilhado, false), y: this.eixoDeValores(empilhado) },
      },
    };
  }

  private configuracaoArea(): ChartConfiguration<'line'> {
    return {
      type: 'line',
      data: {
        labels: this.rotulos(),
        datasets: this.series().map((serie, indice) => {
          const cor = this.corDoPonto(indice, 0);

          return {
            label: serie.nome,
            data: serie.valores,
            borderColor: cor,
            // Área em 10% de opacidade: um véu, nunca um bloco saturado.
            backgroundColor: `${cor}1a`,
            fill: 'origin',
            borderWidth: 2,
            borderCapStyle: 'round',
            borderJoinStyle: 'round',
            // Curva suave que não "afunda" abaixo de zero entre dois meses.
            cubicInterpolationMode: 'monotone',
            pointRadius: 4,
            pointHoverRadius: 6,
            pointHitRadius: 14,
            pointBackgroundColor: cor,
            pointBorderColor: COR_SUPERFICIE,
            pointBorderWidth: 2,
          };
        }),
      },
      options: {
        ...this.opcoesBase(),
        interaction: { mode: 'index', intersect: false },
        scales: { x: this.eixoDeCategorias(false, false), y: this.eixoDeValores(false) },
      },
      plugins: [linhaGuia],
    };
  }

  private configuracaoRosca(): ChartConfiguration<'doughnut'> {
    return {
      type: 'doughnut',
      data: {
        labels: this.rotulos(),
        datasets: this.series().map((serie) => ({
          label: serie.nome,
          data: serie.valores,
          backgroundColor: serie.cor,
          borderColor: COR_SUPERFICIE,
          borderWidth: 2,
          hoverOffset: 4,
        })),
      },
      options: {
        ...this.opcoesBase(),
        cutout: '70%',
        layout: { padding: 6 },
      },
    };
  }

  private opcoesBase() {
    return {
      responsive: true,
      maintainAspectRatio: false,
      animation: { duration: 400 },
      plugins: {
        legend: { display: false },
        tooltip: {
          backgroundColor: '#111',
          titleColor: '#fff',
          bodyColor: '#fff',
          titleFont: { family: FONTE, size: 12, weight: 600 },
          bodyFont: { family: FONTE, size: 12 },
          padding: 10,
          cornerRadius: 6,
          // Marca da série em forma de traço curto, não de caixa.
          boxWidth: 12,
          boxHeight: 3,
          boxPadding: 6,
          callbacks: {
            // O valor vem primeiro: quem passa o mouse já sabe a série e quer o número.
            label: (item: TooltipItem<'bar' | 'line' | 'doughnut'>) => {
              const nome = this.tipo() === 'rosca' ? item.label : item.dataset.label;
              return ` ${this.formatar(Number(item.raw))} · ${nome}`;
            },
            labelColor: (item: TooltipItem<'bar' | 'line' | 'doughnut'>) => {
              const cor = this.corDoPonto(item.datasetIndex, item.dataIndex);
              return { borderColor: cor, backgroundColor: cor, borderWidth: 0 };
            },
          },
        },
      },
    };
  }

  private eixoDeCategorias(empilhado: boolean, horizontal: boolean) {
    return {
      stacked: empilhado,
      grid: { display: false },
      border: { color: COR_EIXO },
      ticks: {
        color: COR_TEXTO_SUAVE,
        font: { family: FONTE, size: 12 },
        autoSkip: !horizontal,
        // Rótulos sempre retos: sem espaço, o Chart.js pula alguns em vez de inclinar.
        maxRotation: 0,
        autoSkipPadding: 10,
        // Nomes longos de cliente encurtados no eixo; o nome inteiro aparece no tooltip.
        callback: (valor: string | number) => encurtar(this.rotulos()[Number(valor)] ?? '', 16),
      },
    };
  }

  private eixoDeValores(empilhado: boolean) {
    return {
      stacked: empilhado,
      beginAtZero: true,
      grid: { color: COR_GRADE },
      border: { display: false },
      ticks: {
        color: COR_TEXTO_SUAVE,
        font: { family: FONTE, size: 12 },
        maxTicksLimit: 5,
        precision: 0,
        callback: (valor: string | number) => this.formatarCompacto(Number(valor)),
      },
    };
  }
}

function encurtar(texto: string, limite: number): string {
  return texto.length > limite ? `${texto.slice(0, limite - 1)}…` : texto;
}
