package br.com.fiadoFacil.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VendaResumoFinanceiroResponse {

    private Long vendaId;
    private BigDecimal totalEmAberto;
    private BigDecimal totalParcelas;
    private Long parcelasPagas;
    private BigDecimal valorEntrada;

    public BigDecimal getValorTotalComJuros() {
        return valorEntrada.add(totalParcelas);
    }
}
