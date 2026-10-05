package br.com.fiadoFacil.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** Linha da lista de cobrança: uma parcela vencida e ainda não paga. */
@Getter
@Builder
@AllArgsConstructor
public class ParcelaEmAtrasoResponse {

    private Long parcelaId;
    private Long vendaId;
    private Long clienteId;
    private String clienteNome;
    private String clienteTelefone;
    private Integer numero;
    private Integer quantidadeParcelas;
    private BigDecimal valor;
    private LocalDate dataVencimento;
    private long diasEmAtraso;
}
