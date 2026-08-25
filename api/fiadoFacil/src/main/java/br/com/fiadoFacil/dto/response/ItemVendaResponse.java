package br.com.fiadoFacil.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ItemVendaResponse {

    private Long id;
    private String nome;
    private Integer quantidade;
    private BigDecimal valorUnitario;
    private BigDecimal valorTotal;
}