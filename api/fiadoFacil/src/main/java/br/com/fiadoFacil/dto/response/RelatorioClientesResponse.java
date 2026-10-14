package br.com.fiadoFacil.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RelatorioClientesResponse {

    private long totalClientes;
    private long clientesSemDebito;

    private long clientesDevendo;

    private long clientesNoPrazo;

    private long clientesEmAtraso;

    private List<ClienteSaldoResponse> maioresDevedores;
    private List<ParcelaEmAtrasoResponse> parcelasEmAtraso;
}
