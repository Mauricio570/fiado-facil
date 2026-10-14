package br.com.fiadoFacil.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.fiadoFacil.domain.enums.FormaPagamento;
import br.com.fiadoFacil.domain.enums.StatusVenda;
import lombok.Getter;

@Getter
public class RelatorioVendaResponse {

    private final Long vendaId;
    private final LocalDateTime dataCriacao;
    private final Long clienteId;
    private final String clienteNome;
    private final StatusVenda status;
    private final FormaPagamento formaPagamento;
    private final Integer quantidadeParcelas;
    private final BigDecimal valorEntrada;
    private final BigDecimal valorTotalComJuros;
    private final BigDecimal valorPago;
    private final BigDecimal valorAVencer;
    private final BigDecimal valorEmAtraso;

    public RelatorioVendaResponse(Long vendaId,
                                  LocalDateTime dataCriacao,
                                  Long clienteId,
                                  String clienteNome,
                                  StatusVenda status,
                                  FormaPagamento formaPagamento,
                                  Integer quantidadeParcelas,
                                  BigDecimal valorEntrada,
                                  BigDecimal totalParcelas,
                                  BigDecimal totalParcelasPagas,
                                  BigDecimal totalParcelasEmAtraso) {
        this.vendaId = vendaId;
        this.dataCriacao = dataCriacao;
        this.clienteId = clienteId;
        this.clienteNome = clienteNome;
        this.status = status;
        this.formaPagamento = formaPagamento;
        this.quantidadeParcelas = quantidadeParcelas;
        this.valorEntrada = valorEntrada;
        this.valorTotalComJuros = valorEntrada.add(totalParcelas);
        this.valorPago = valorEntrada.add(totalParcelasPagas);
        this.valorEmAtraso = totalParcelasEmAtraso;
        this.valorAVencer = totalParcelas.subtract(totalParcelasPagas).subtract(totalParcelasEmAtraso);
    }
}
