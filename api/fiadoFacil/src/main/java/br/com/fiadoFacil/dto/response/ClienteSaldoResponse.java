package br.com.fiadoFacil.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ClienteSaldoResponse {

    private Long clienteId;
    private String nome;
    private BigDecimal valorAVencer;
    private BigDecimal valorEmAtraso;
    private BigDecimal totalEmAberto;
}
