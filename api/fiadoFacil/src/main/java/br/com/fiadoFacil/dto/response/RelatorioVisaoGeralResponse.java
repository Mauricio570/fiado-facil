package br.com.fiadoFacil.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

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

    private BigDecimal percentualInadimplencia;

    private List<RelatorioMesResponse> meses;
    private List<PrevisaoRecebimentoResponse> previsaoRecebimento;
}
