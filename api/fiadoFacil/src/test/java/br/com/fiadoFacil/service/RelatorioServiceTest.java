package br.com.fiadoFacil.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import br.com.fiadoFacil.domain.Cliente;
import br.com.fiadoFacil.domain.Pagamento;
import br.com.fiadoFacil.domain.Parcela;
import br.com.fiadoFacil.domain.Usuario;
import br.com.fiadoFacil.domain.Venda;
import br.com.fiadoFacil.domain.enums.FormaPagamento;
import br.com.fiadoFacil.domain.enums.StatusParcela;
import br.com.fiadoFacil.domain.enums.StatusVenda;
import br.com.fiadoFacil.dto.response.ClienteSaldoResponse;
import br.com.fiadoFacil.dto.response.ParcelaEmAtrasoResponse;
import br.com.fiadoFacil.dto.response.RelatorioClientesResponse;
import br.com.fiadoFacil.dto.response.RelatorioMesResponse;
import br.com.fiadoFacil.dto.response.RelatorioVendaResponse;
import br.com.fiadoFacil.dto.response.RelatorioVisaoGeralResponse;
import br.com.fiadoFacil.mapper.RelatorioMapper;
import br.com.fiadoFacil.repository.ClienteRepository;
import br.com.fiadoFacil.repository.PagamentoRepository;
import br.com.fiadoFacil.repository.ParcelaRepository;

/**
 * Regras de cálculo dos relatórios, com os repositórios simulados. As datas
 * são relativas a hoje porque o service usa a data atual para decidir o que
 * está em atraso.
 */
@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    private static final Long USUARIO_ID = 1L;

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private ParcelaRepository parcelaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    private RelatorioService relatorioService;

    private final LocalDate hoje = LocalDate.now();

    @BeforeEach
    void configurar() {
        relatorioService = new RelatorioService(pagamentoRepository, parcelaRepository, clienteRepository,
                new RelatorioMapper(), usuarioAutenticadoService);

        lenient().when(usuarioAutenticadoService.get()).thenReturn(Usuario.builder().id(USUARIO_ID).build());
    }

    @Test
    void clientesSeparaSaldoAVencerEmAtrasoEOrdenaMaioresDevedores() {
        Cliente maria = cliente(1L, "Maria");
        Cliente joao = cliente(2L, "João");
        Cliente ana = cliente(3L, "Ana");

        Parcela mariaAtrasada = parcelaEmAberto(10L, maria, "50.00", hoje.minusDays(10));
        Parcela mariaAVencer = parcelaEmAberto(11L, maria, "30.00", hoje.plusDays(20));
        Parcela joaoVenceHoje = parcelaEmAberto(12L, joao, "100.00", hoje);
        Parcela anaAtrasada = parcelaEmAberto(13L, ana, "20.00", hoje.minusDays(2));

        when(parcelaRepository.buscarPorStatusComCliente(USUARIO_ID, StatusParcela.EM_ABERTO))
                .thenReturn(List.of(anaAtrasada, mariaAVencer, joaoVenceHoje, mariaAtrasada));
        when(clienteRepository.countByUsuarioId(USUARIO_ID)).thenReturn(5L);

        RelatorioClientesResponse relatorio = relatorioService.buscarClientes();

        assertThat(relatorio.getTotalClientes()).isEqualTo(5);
        assertThat(relatorio.getClientesDevendo()).isEqualTo(3);
        assertThat(relatorio.getClientesEmAtraso()).isEqualTo(2);
        assertThat(relatorio.getClientesNoPrazo()).isEqualTo(1);
        assertThat(relatorio.getClientesSemDebito()).isEqualTo(2);

        assertThat(relatorio.getMaioresDevedores())
                .extracting(ClienteSaldoResponse::getNome)
                .containsExactly("João", "Maria", "Ana");
        assertThat(relatorio.getMaioresDevedores().get(1).getValorEmAtraso()).isEqualByComparingTo("50.00");
        assertThat(relatorio.getMaioresDevedores().get(1).getValorAVencer()).isEqualByComparingTo("30.00");
        // Parcela que vence hoje ainda não está atrasada.
        assertThat(relatorio.getMaioresDevedores().get(0).getValorEmAtraso()).isEqualByComparingTo("0");

        assertThat(relatorio.getParcelasEmAtraso())
                .extracting(ParcelaEmAtrasoResponse::getClienteNome, ParcelaEmAtrasoResponse::getDiasEmAtraso)
                .containsExactly(
                        tuple("Maria", 10L),
                        tuple("Ana", 2L));
    }

    @Test
    void visaoGeralPreencheMesesSemMovimentoESeparaAtrasoDaPrevisao() {
        LocalDate inicio = YearMonth.from(hoje).minusMonths(2).atDay(1);
        Cliente maria = cliente(1L, "Maria");

        // Venda de dois meses atrás: R$ 10 de entrada + R$ 90 em parcelas,
        // das quais R$ 30 pagas e R$ 30 vencidas sem pagamento.
        RelatorioVendaResponse venda = new RelatorioVendaResponse(1L, inicio.atTime(10, 0), 1L, "Maria",
                StatusVenda.EM_ABERTO, FormaPagamento.CREDITO, 3, new BigDecimal("10.00"),
                new BigDecimal("90.00"), new BigDecimal("30.00"), new BigDecimal("30.00"));
        Parcela pagaHoje = parcela(20L, maria, "30.00", hoje.minusDays(1), StatusParcela.PAGO, hoje);
        Parcela atrasada = parcelaEmAberto(21L, maria, "30.00", hoje.minusDays(5));
        Parcela aVencer = parcelaEmAberto(22L, maria, "30.00", hoje.plusMonths(1));

        when(pagamentoRepository.buscarVendasDoPeriodo(eq(USUARIO_ID), eq(inicio.atStartOfDay()),
                eq(hoje.atTime(LocalTime.MAX)), eq(hoje), eq(StatusParcela.PAGO), eq(StatusParcela.EM_ABERTO)))
                .thenReturn(List.of(venda));
        when(parcelaRepository.buscarPorStatusEPeriodoDePagamento(USUARIO_ID, StatusParcela.PAGO, inicio, hoje))
                .thenReturn(List.of(pagaHoje));
        when(parcelaRepository.buscarPorStatusComCliente(USUARIO_ID, StatusParcela.EM_ABERTO))
                .thenReturn(List.of(atrasada, aVencer));
        when(parcelaRepository.somarComVencimentoAntesDe(USUARIO_ID, hoje)).thenReturn(new BigDecimal("90.00"));
        when(pagamentoRepository.somarEntradasNoPeriodo(eq(USUARIO_ID), any(LocalDateTime.class),
                any(LocalDateTime.class))).thenReturn(BigDecimal.ZERO);
        when(parcelaRepository.somarPorStatusEPeriodo(eq(USUARIO_ID), eq(StatusParcela.PAGO),
                any(LocalDate.class), any(LocalDate.class))).thenReturn(new BigDecimal("30.00"));

        RelatorioVisaoGeralResponse relatorio = relatorioService.buscarVisaoGeral(inicio, hoje);

        assertThat(relatorio.getTotalVendido()).isEqualByComparingTo("100.00");
        assertThat(relatorio.getTotalRecebido()).isEqualByComparingTo("40.00");
        assertThat(relatorio.getRecebidoEsteMes()).isEqualByComparingTo("30.00");
        assertThat(relatorio.getTicketMedio()).isEqualByComparingTo("100.00");
        assertThat(relatorio.getTotalAReceber()).isEqualByComparingTo("60.00");
        assertThat(relatorio.getTotalEmAtraso()).isEqualByComparingTo("30.00");
        assertThat(relatorio.getQuantidadeParcelasEmAtraso()).isEqualTo(1);
        assertThat(relatorio.getPercentualInadimplencia()).isEqualByComparingTo("33.3");

        List<RelatorioMesResponse> meses = relatorio.getMeses();
        assertThat(meses).extracting(RelatorioMesResponse::getMes)
                .containsExactly(YearMonth.from(inicio), YearMonth.from(inicio).plusMonths(1), YearMonth.from(hoje));

        RelatorioMesResponse mesDaVenda = meses.get(0);
        assertThat(mesDaVenda.getValorVendido()).isEqualByComparingTo("100.00");
        assertThat(mesDaVenda.getValorPago()).isEqualByComparingTo("40.00");
        assertThat(mesDaVenda.getValorAVencer()).isEqualByComparingTo("30.00");
        assertThat(mesDaVenda.getValorEmAtraso()).isEqualByComparingTo("30.00");
        assertThat(mesDaVenda.getValorRecebido()).isEqualByComparingTo("10.00");

        assertThat(meses.get(1).getValorVendido()).isEqualByComparingTo("0");
        assertThat(meses.get(2).getValorRecebido()).isEqualByComparingTo("30.00");

        // A parcela atrasada fica fora da previsão; a que vence no mês que vem entra nele.
        assertThat(relatorio.getPrevisaoRecebimento()).hasSize(6);
        assertThat(relatorio.getPrevisaoRecebimento().get(0).getMes()).isEqualTo(YearMonth.from(hoje));
        assertThat(relatorio.getPrevisaoRecebimento().get(0).getValor()).isEqualByComparingTo("0");
        assertThat(relatorio.getPrevisaoRecebimento().get(1).getValor()).isEqualByComparingTo("30.00");
    }

    @Test
    void inadimplenciaFicaNulaQuandoNenhumaParcelaVenceu() {
        when(parcelaRepository.somarComVencimentoAntesDe(USUARIO_ID, hoje)).thenReturn(BigDecimal.ZERO);
        when(pagamentoRepository.somarEntradasNoPeriodo(eq(USUARIO_ID), any(LocalDateTime.class),
                any(LocalDateTime.class))).thenReturn(BigDecimal.ZERO);
        when(parcelaRepository.somarPorStatusEPeriodo(eq(USUARIO_ID), eq(StatusParcela.PAGO),
                any(LocalDate.class), any(LocalDate.class))).thenReturn(BigDecimal.ZERO);

        RelatorioVisaoGeralResponse relatorio = relatorioService.buscarVisaoGeral(null, null);

        assertThat(relatorio.getPercentualInadimplencia()).isNull();
        assertThat(relatorio.getTicketMedio()).isEqualByComparingTo("0");
    }

    @Test
    void semDatasUsaOsUltimosSeisMeses() {
        relatorioService.listarVendas(null, null);

        LocalDate inicioEsperado = YearMonth.from(hoje).minusMonths(5).atDay(1);
        verify(pagamentoRepository).buscarVendasDoPeriodo(eq(USUARIO_ID), eq(inicioEsperado.atStartOfDay()),
                eq(hoje.atTime(LocalTime.MAX)), eq(hoje), eq(StatusParcela.PAGO), eq(StatusParcela.EM_ABERTO));
    }

    @Test
    void periodoComDuasDatasDentroDoLimiteEhAceito() {
        LocalDate inicio = LocalDate.of(2025, 1, 1);
        LocalDate fim = LocalDate.of(2026, 12, 31);

        relatorioService.listarVendas(inicio, fim);

        verify(pagamentoRepository).buscarVendasDoPeriodo(anyLong(), eq(inicio.atStartOfDay()),
                eq(fim.atTime(LocalTime.MAX)), any(LocalDate.class), any(), any());
    }

    @Test
    void periodoInvalidoRetornaErro400ComMensagem() {
        assertErroDePeriodo(LocalDate.of(2026, 1, 1), null,
                "Informe a data inicial e a data final do período.");
        assertErroDePeriodo(LocalDate.of(2026, 5, 2), LocalDate.of(2026, 5, 1),
                "A data inicial não pode ser depois da data final.");
        assertErroDePeriodo(LocalDate.of(2025, 1, 1), LocalDate.of(2027, 1, 1),
                "O período do relatório pode ter no máximo 24 meses.");
    }

    private void assertErroDePeriodo(LocalDate inicio, LocalDate fim, String mensagem) {
        assertThatThrownBy(() -> relatorioService.listarVendas(inicio, fim))
                .isInstanceOfSatisfying(ResponseStatusException.class, erro -> {
                    assertThat(erro.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(erro.getReason()).isEqualTo(mensagem);
                });
    }

    private Cliente cliente(Long id, String nome) {
        return Cliente.builder().id(id).nome(nome).telefone("(51) 99999-0000").build();
    }

    private Parcela parcelaEmAberto(Long id, Cliente cliente, String valor, LocalDate vencimento) {
        return parcela(id, cliente, valor, vencimento, StatusParcela.EM_ABERTO, null);
    }

    private Parcela parcela(Long id, Cliente cliente, String valor, LocalDate vencimento,
                            StatusParcela status, LocalDate dataPagamento) {
        Venda venda = Venda.builder().id(id * 100).cliente(cliente).status(StatusVenda.EM_ABERTO).build();
        Pagamento pagamento = Pagamento.builder().id(id * 100).venda(venda)
                .formaPagamento(FormaPagamento.CREDITO).quantidadeParcelas(3).build();

        return Parcela.builder()
                .id(id)
                .pagamento(pagamento)
                .numero(1)
                .valor(new BigDecimal(valor))
                .dataVencimento(vencimento)
                .dataPagamento(dataPagamento)
                .status(status)
                .build();
    }
}
