package br.com.fiadoFacil.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Aba "Clientes" dos relatórios. Mostra sempre a situação de hoje: dívida é
 * um estado atual, então o período escolhido na tela não se aplica aqui.
 */
@Getter
@Builder
@AllArgsConstructor
public class RelatorioClientesResponse {

    private long totalClientes;
    private long clientesSemDebito;

    /** Clientes com alguma parcela em aberto (soma de clientesNoPrazo e clientesEmAtraso). */
    private long clientesDevendo;

    /** Devem, mas nenhuma parcela venceu ainda. */
    private long clientesNoPrazo;

    /** Têm pelo menos uma parcela vencida e não paga. */
    private long clientesEmAtraso;

    private List<ClienteSaldoResponse> maioresDevedores;
    private List<ParcelaEmAtrasoResponse> parcelasEmAtraso;
}
