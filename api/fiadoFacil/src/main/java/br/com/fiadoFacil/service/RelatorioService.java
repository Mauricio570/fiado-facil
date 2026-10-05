package br.com.fiadoFacil.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.fiadoFacil.domain.Cliente;
import br.com.fiadoFacil.domain.Parcela;
import br.com.fiadoFacil.domain.enums.StatusParcela;
import br.com.fiadoFacil.dto.response.ClienteSaldoResponse;
import br.com.fiadoFacil.dto.response.ParcelaEmAtrasoResponse;
import br.com.fiadoFacil.dto.response.PrevisaoRecebimentoResponse;
import br.com.fiadoFacil.dto.response.RelatorioClientesResponse;
import br.com.fiadoFacil.dto.response.RelatorioMesResponse;
import br.com.fiadoFacil.dto.response.RelatorioVendaResponse;
import br.com.fiadoFacil.dto.response.RelatorioVisaoGeralResponse;
import br.com.fiadoFacil.mapper.RelatorioMapper;
import br.com.fiadoFacil.repository.ClienteRepository;
import br.com.fiadoFacil.repository.PagamentoRepository;
import br.com.fiadoFacil.repository.ParcelaRepository;

/**
 * Relatórios do comerciante. Todo valor de venda aqui é o valor final cobrado
 * do cliente, já com juros (entrada + parcelas). Uma parcela está em atraso
 * quando continua em aberto depois do dia do vencimento, sem tolerância.
 */
@Service
public class RelatorioService {

    /** Período usado quando a tela não informa datas: o mês atual e os cinco anteriores. */
    private static final int MESES_PADRAO = 6;

    /** Limite do período, para os gráficos mensais continuarem legíveis. */
    private static final int MESES_MAXIMOS = 24;

    /** Quantos meses a previsão de recebimento cobre, contando o mês atual. */
    private static final int MESES_PREVISAO = 6;

    private static final int LIMITE_MAIORES_DEVEDORES = 5;

    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private final PagamentoRepository pagamentoRepository;
    private final ParcelaRepository parcelaRepository;
    private final ClienteRepository clienteRepository;
    private final RelatorioMapper relatorioMapper;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public RelatorioService(PagamentoRepository pagamentoRepository,
                            ParcelaRepository parcelaRepository,
                            ClienteRepository clienteRepository,
                            RelatorioMapper relatorioMapper,
                            UsuarioAutenticadoService usuarioAutenticadoService) {
        this.pagamentoRepository = pagamentoRepository;
        this.parcelaRepository = parcelaRepository;
        this.clienteRepository = clienteRepository;
        this.relatorioMapper = relatorioMapper;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @Transactional(readOnly = true)
    public RelatorioVisaoGeralResponse buscarVisaoGeral(LocalDate inicio, LocalDate fim) {
        Periodo periodo = resolverPeriodo(inicio, fim);
        Long usuarioId = usuarioAutenticadoService.get().getId();
        LocalDate hoje = LocalDate.now();

        List<RelatorioVendaResponse> vendas = buscarVendas(usuarioId, periodo, hoje);
        List<Parcela> parcelasPagas = parcelaRepository.buscarPorStatusEPeriodoDePagamento(
                usuarioId, StatusParcela.PAGO, periodo.inicio(), periodo.fim());
        List<Parcela> parcelasEmAberto = parcelaRepository.buscarPorStatusComCliente(
                usuarioId, StatusParcela.EM_ABERTO);
        List<Parcela> parcelasEmAtraso = parcelasEmAberto.stream()
                .filter(parcela -> estaAtrasada(parcela, hoje))
                .toList();

        BigDecimal totalVendido = somar(vendas, RelatorioVendaResponse::getValorTotalComJuros);
        BigDecimal totalRecebido = somar(vendas, RelatorioVendaResponse::getValorEntrada)
                .add(somar(parcelasPagas, Parcela::getValor));
        BigDecimal totalEmAtraso = somar(parcelasEmAtraso, Parcela::getValor);

        return RelatorioVisaoGeralResponse.builder()
                .inicio(periodo.inicio())
                .fim(periodo.fim())
                .totalVendido(totalVendido)
                .totalRecebido(totalRecebido)
                .recebidoEsteMes(calcularRecebidoNoMes(usuarioId, YearMonth.from(hoje)))
                .quantidadeVendas(vendas.size())
                .ticketMedio(calcularMedia(totalVendido, vendas.size()))
                .totalAReceber(somar(parcelasEmAberto, Parcela::getValor))
                .totalEmAtraso(totalEmAtraso)
                .quantidadeParcelasEmAtraso(parcelasEmAtraso.size())
                .percentualInadimplencia(calcularInadimplencia(usuarioId, totalEmAtraso, hoje))
                .meses(montarMeses(periodo, vendas, parcelasPagas))
                .previsaoRecebimento(montarPrevisao(parcelasEmAberto, hoje))
                .build();
    }

    @Transactional(readOnly = true)
    public RelatorioClientesResponse buscarClientes() {
        Long usuarioId = usuarioAutenticadoService.get().getId();
        LocalDate hoje = LocalDate.now();

        List<Parcela> parcelasEmAberto = parcelaRepository.buscarPorStatusComCliente(
                usuarioId, StatusParcela.EM_ABERTO);

        List<ClienteSaldoResponse> saldos = parcelasEmAberto.stream()
                .collect(Collectors.groupingBy(parcela -> clienteDa(parcela).getId()))
                .values()
                .stream()
                .map(parcelasDoCliente -> montarSaldo(parcelasDoCliente, hoje))
                .sorted(Comparator.comparing(ClienteSaldoResponse::getTotalEmAberto, Comparator.reverseOrder())
                        .thenComparing(ClienteSaldoResponse::getNome))
                .toList();

        List<ParcelaEmAtrasoResponse> parcelasEmAtraso = parcelasEmAberto.stream()
                .filter(parcela -> estaAtrasada(parcela, hoje))
                .sorted(Comparator.comparing(Parcela::getDataVencimento)
                        .thenComparing(parcela -> clienteDa(parcela).getNome()))
                .map(parcela -> relatorioMapper.toParcelaEmAtrasoResponse(parcela, hoje))
                .toList();

        long totalClientes = clienteRepository.countByUsuarioId(usuarioId);
        long clientesEmAtraso = saldos.stream()
                .filter(saldo -> saldo.getValorEmAtraso().signum() > 0)
                .count();

        return RelatorioClientesResponse.builder()
                .totalClientes(totalClientes)
                .clientesSemDebito(totalClientes - saldos.size())
                .clientesDevendo(saldos.size())
                .clientesNoPrazo(saldos.size() - clientesEmAtraso)
                .clientesEmAtraso(clientesEmAtraso)
                .maioresDevedores(saldos.stream().limit(LIMITE_MAIORES_DEVEDORES).toList())
                .parcelasEmAtraso(parcelasEmAtraso)
                .build();
    }

    @Transactional(readOnly = true)
    public List<RelatorioVendaResponse> listarVendas(LocalDate inicio, LocalDate fim) {
        Periodo periodo = resolverPeriodo(inicio, fim);
        Long usuarioId = usuarioAutenticadoService.get().getId();

        return buscarVendas(usuarioId, periodo, LocalDate.now());
    }

    // Sem datas, o relatório mostra os últimos seis meses. Com datas, as duas
    // são obrigatórias, a inicial não pode passar da final e o intervalo tem
    // um limite de meses.
    private Periodo resolverPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio == null && fim == null) {
            LocalDate hoje = LocalDate.now();
            return new Periodo(YearMonth.from(hoje).minusMonths(MESES_PADRAO - 1L).atDay(1), hoje);
        }

        if (inicio == null || fim == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe a data inicial e a data final do período.");
        }

        if (inicio.isAfter(fim)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A data inicial não pode ser depois da data final.");
        }

        long quantidadeMeses = ChronoUnit.MONTHS.between(YearMonth.from(inicio), YearMonth.from(fim)) + 1;
        if (quantidadeMeses > MESES_MAXIMOS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O período do relatório pode ter no máximo " + MESES_MAXIMOS + " meses.");
        }

        return new Periodo(inicio, fim);
    }

    private List<RelatorioVendaResponse> buscarVendas(Long usuarioId, Periodo periodo, LocalDate hoje) {
        return pagamentoRepository.buscarVendasDoPeriodo(
                usuarioId,
                periodo.inicio().atStartOfDay(),
                periodo.fim().atTime(LocalTime.MAX),
                hoje,
                StatusParcela.PAGO,
                StatusParcela.EM_ABERTO);
    }

    // Um item por mês do período, inclusive os meses sem movimento, para o
    // gráfico não pular meses. As colunas de venda olham para as vendas feitas
    // no mês; o recebido soma as entradas dessas vendas e as parcelas pagas no
    // mês, de qualquer venda — a mesma regra do "Recebido este mês" da Home.
    private List<RelatorioMesResponse> montarMeses(Periodo periodo,
                                                   List<RelatorioVendaResponse> vendas,
                                                   List<Parcela> parcelasPagas) {
        Map<YearMonth, List<RelatorioVendaResponse>> vendasPorMes = vendas.stream()
                .collect(Collectors.groupingBy(venda -> YearMonth.from(venda.getDataCriacao())));
        Map<YearMonth, List<Parcela>> parcelasPagasPorMes = parcelasPagas.stream()
                .collect(Collectors.groupingBy(parcela -> YearMonth.from(parcela.getDataPagamento())));

        YearMonth ultimoMes = YearMonth.from(periodo.fim());

        return Stream.iterate(YearMonth.from(periodo.inicio()), mes -> !mes.isAfter(ultimoMes), mes -> mes.plusMonths(1))
                .map(mes -> {
                    List<RelatorioVendaResponse> vendasDoMes = vendasPorMes.getOrDefault(mes, List.of());
                    List<Parcela> parcelasDoMes = parcelasPagasPorMes.getOrDefault(mes, List.of());

                    return RelatorioMesResponse.builder()
                            .mes(mes)
                            .valorVendido(somar(vendasDoMes, RelatorioVendaResponse::getValorTotalComJuros))
                            .valorPago(somar(vendasDoMes, RelatorioVendaResponse::getValorPago))
                            .valorAVencer(somar(vendasDoMes, RelatorioVendaResponse::getValorAVencer))
                            .valorEmAtraso(somar(vendasDoMes, RelatorioVendaResponse::getValorEmAtraso))
                            .valorRecebido(somar(vendasDoMes, RelatorioVendaResponse::getValorEntrada)
                                    .add(somar(parcelasDoMes, Parcela::getValor)))
                            .build();
                })
                .toList();
    }

    // Parcelas que ainda vão vencer, agrupadas pelo mês do vencimento. As já
    // atrasadas ficam de fora: elas aparecem separadas, no total em atraso.
    private List<PrevisaoRecebimentoResponse> montarPrevisao(List<Parcela> parcelasEmAberto, LocalDate hoje) {
        Map<YearMonth, List<Parcela>> aVencerPorMes = parcelasEmAberto.stream()
                .filter(parcela -> !estaAtrasada(parcela, hoje))
                .collect(Collectors.groupingBy(parcela -> YearMonth.from(parcela.getDataVencimento())));

        return Stream.iterate(YearMonth.from(hoje), mes -> mes.plusMonths(1))
                .limit(MESES_PREVISAO)
                .map(mes -> {
                    List<Parcela> parcelasDoMes = aVencerPorMes.getOrDefault(mes, List.of());

                    return PrevisaoRecebimentoResponse.builder()
                            .mes(mes)
                            .valor(somar(parcelasDoMes, Parcela::getValor))
                            .quantidadeParcelas(parcelasDoMes.size())
                            .build();
                })
                .toList();
    }

    private ClienteSaldoResponse montarSaldo(List<Parcela> parcelasDoCliente, LocalDate hoje) {
        Cliente cliente = clienteDa(parcelasDoCliente.get(0));
        BigDecimal totalEmAberto = somar(parcelasDoCliente, Parcela::getValor);
        BigDecimal valorEmAtraso = somar(
                parcelasDoCliente.stream().filter(parcela -> estaAtrasada(parcela, hoje)).toList(),
                Parcela::getValor);

        return ClienteSaldoResponse.builder()
                .clienteId(cliente.getId())
                .nome(cliente.getNome())
                .valorAVencer(totalEmAberto.subtract(valorEmAtraso))
                .valorEmAtraso(valorEmAtraso)
                .totalEmAberto(totalEmAberto)
                .build();
    }

    // Mesma conta do "Recebido este mês" da Home (PainelService), para os
    // dois números baterem.
    private BigDecimal calcularRecebidoNoMes(Long usuarioId, YearMonth mes) {
        BigDecimal entradas = pagamentoRepository.somarEntradasNoPeriodo(
                usuarioId, mes.atDay(1).atStartOfDay(), mes.atEndOfMonth().atTime(LocalTime.MAX));
        BigDecimal parcelasPagas = parcelaRepository.somarPorStatusEPeriodo(
                usuarioId, StatusParcela.PAGO, mes.atDay(1), mes.atEndOfMonth());

        return entradas.add(parcelasPagas);
    }

    // Do total que já venceu (pago ou não), quanto ficou sem pagar.
    private BigDecimal calcularInadimplencia(Long usuarioId, BigDecimal totalEmAtraso, LocalDate hoje) {
        BigDecimal totalVencido = parcelaRepository.somarComVencimentoAntesDe(usuarioId, hoje);
        if (totalVencido.signum() == 0) {
            return null;
        }

        return totalEmAtraso.multiply(CEM).divide(totalVencido, 1, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularMedia(BigDecimal total, int quantidade) {
        if (quantidade == 0) {
            return BigDecimal.ZERO;
        }

        return total.divide(BigDecimal.valueOf(quantidade), 2, RoundingMode.HALF_UP);
    }

    private boolean estaAtrasada(Parcela parcela, LocalDate hoje) {
        return parcela.getStatus() == StatusParcela.EM_ABERTO && parcela.getDataVencimento().isBefore(hoje);
    }

    private Cliente clienteDa(Parcela parcela) {
        return parcela.getPagamento().getVenda().getCliente();
    }

    private static <T> BigDecimal somar(List<T> itens, Function<T, BigDecimal> valor) {
        return itens.stream().map(valor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private record Periodo(LocalDate inicio, LocalDate fim) {
    }
}
