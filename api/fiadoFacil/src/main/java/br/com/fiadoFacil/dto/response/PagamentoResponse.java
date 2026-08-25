package br.com.fiadoFacil.dto.response;

import java.math.BigDecimal;
import java.util.List;

import br.com.fiadoFacil.domain.enums.FormaPagamento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PagamentoResponse {

    private Long id;
    private FormaPagamento formaPagamento;
    private Integer quantidadeParcelas;
    private BigDecimal jurosMes;
    private BigDecimal valorEntrada;
    private List<ParcelaResponse> parcelas;
}