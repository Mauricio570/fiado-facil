package br.com.fiadoFacil.dto.response;

import java.math.BigDecimal;
import java.time.YearMonth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RelatorioMesResponse {

    private YearMonth mes;
    private BigDecimal valorVendido;
    private BigDecimal valorPago;
    private BigDecimal valorAVencer;
    private BigDecimal valorEmAtraso;
    private BigDecimal valorRecebido;
}
