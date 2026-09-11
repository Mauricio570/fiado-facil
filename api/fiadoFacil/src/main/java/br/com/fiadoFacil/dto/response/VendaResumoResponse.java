package br.com.fiadoFacil.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.fiadoFacil.domain.enums.StatusVenda;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class VendaResumoResponse {

    private Long id;
    private StatusVenda status;
    private LocalDateTime dataCriacao;
    private BigDecimal valorTotal;
    private BigDecimal valorTotalComJuros;
    private BigDecimal totalEmAberto;

    /**
     * Indica se a venda ainda pode ser editada ou excluída — falso assim que
     * qualquer pagamento é registrado (entrada, parcela paga ou venda quitada).
     */
    private boolean podeAlterar;
}
