package br.com.fiadoFacil.dto.response;

import java.math.BigDecimal;
import java.time.YearMonth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Números de um mês do período do relatório.
 *
 * Os campos vendido, pago, a vencer e em atraso olham para as vendas feitas
 * no mês (vendido = pago + a vencer + em atraso). Já o recebido é o dinheiro
 * que entrou no mês, mesmo que seja parcela de uma venda antiga.
 */
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
