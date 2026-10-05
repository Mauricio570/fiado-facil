package br.com.fiadoFacil.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Aba "Visão geral" dos relatórios. Parte dos números depende do período
 * escolhido (vendido, recebido, ticket médio e os meses) e parte mostra a
 * situação de hoje, independente do período (a receber, atraso,
 * inadimplência e previsão).
 */
@Getter
@Builder
@AllArgsConstructor
public class RelatorioVisaoGeralResponse {

    private LocalDate inicio;
    private LocalDate fim;

    private BigDecimal totalVendido;
    private BigDecimal totalRecebido;
    private BigDecimal recebidoEsteMes;
    private long quantidadeVendas;
    private BigDecimal ticketMedio;

    private BigDecimal totalAReceber;
    private BigDecimal totalEmAtraso;
    private long quantidadeParcelasEmAtraso;

    /**
     * Percentual do valor já vencido que não foi pago. Nulo quando nenhuma
     * parcela venceu ainda, porque aí não há base para calcular.
     */
    private BigDecimal percentualInadimplencia;

    private List<RelatorioMesResponse> meses;
    private List<PrevisaoRecebimentoResponse> previsaoRecebimento;
}
