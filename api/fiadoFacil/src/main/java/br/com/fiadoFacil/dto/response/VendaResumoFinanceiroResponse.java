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

    /**
     * Total que o cliente paga pela venda, já com juros: a entrada mais a soma
     * de todas as parcelas geradas. Na venda à vista não há parcelas e o valor
     * cai na entrada, então o resultado é o próprio valor dos produtos.
     */
    public BigDecimal getValorTotalComJuros() {
        return valorEntrada.add(totalParcelas);
    }
}
