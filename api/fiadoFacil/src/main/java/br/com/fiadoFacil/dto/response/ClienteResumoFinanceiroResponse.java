package br.com.fiadoFacil.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClienteResumoFinanceiroResponse {

    private Long clienteId;
    private BigDecimal totalEmAberto;
    private LocalDateTime ultimaCompra;
}
