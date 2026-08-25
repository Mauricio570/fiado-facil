package br.com.fiadoFacil.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VendaRequest {

    @NotNull
    private Long fkCliente;

    @NotEmpty
    @Valid
    private List<ItemVendaRequest> itens;

    @NotNull
    @Valid
    private PagamentoRequest pagamento;
}