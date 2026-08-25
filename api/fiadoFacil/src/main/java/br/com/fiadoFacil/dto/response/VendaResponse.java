package br.com.fiadoFacil.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import br.com.fiadoFacil.domain.enums.StatusVenda;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class VendaResponse {

    private Long id;
    private Long clienteId;
    private String clienteNome;
    private StatusVenda status;
    private LocalDateTime dataCriacao;
    private BigDecimal valorTotal;
    private List<ItemVendaResponse> itens;
    private PagamentoResponse pagamento;
}