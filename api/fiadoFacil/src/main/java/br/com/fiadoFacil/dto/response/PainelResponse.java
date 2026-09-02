package br.com.fiadoFacil.dto.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PainelResponse {

    private BigDecimal totalAReceber;
    private BigDecimal recebidoEsteMes;
    private long clientesEmDebito;
    private long totalClientes;
    private List<ClienteEmDebitoResponse> clientesComContasEmAberto;
}