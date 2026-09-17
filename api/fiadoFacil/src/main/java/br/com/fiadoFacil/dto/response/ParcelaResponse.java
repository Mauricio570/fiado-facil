package br.com.fiadoFacil.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.com.fiadoFacil.domain.enums.StatusParcela;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ParcelaResponse {

    private Long id;
    private Integer numero;
    private BigDecimal valor;
    private LocalDate dataVencimento;
    private LocalDate dataPagamento;
    private StatusParcela status;
}