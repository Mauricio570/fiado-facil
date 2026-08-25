package br.com.fiadoFacil.dto.request;

import java.math.BigDecimal;

import br.com.fiadoFacil.domain.enums.FormaPagamento;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PagamentoRequest {

    @NotNull
    private FormaPagamento formaPagamento;

    private Integer quantidadeParcelas;
    private BigDecimal jurosMes;
    private BigDecimal valorEntrada;
}