package br.com.fiadoFacil.dto.response;

import java.math.BigDecimal;
import java.time.YearMonth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** Quanto está previsto para entrar em um mês, pelas parcelas que ainda vão vencer nele. */
@Getter
@Builder
@AllArgsConstructor
public class PrevisaoRecebimentoResponse {

    private YearMonth mes;
    private BigDecimal valor;
    private long quantidadeParcelas;
}
