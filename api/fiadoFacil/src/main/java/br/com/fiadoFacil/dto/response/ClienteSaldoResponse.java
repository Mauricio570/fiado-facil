package br.com.fiadoFacil.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** Saldo em aberto de um cliente, separando o que ainda vai vencer do que já está atrasado. */
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
