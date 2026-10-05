package br.com.fiadoFacil.mapper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;

import br.com.fiadoFacil.domain.Cliente;
import br.com.fiadoFacil.domain.Pagamento;
import br.com.fiadoFacil.domain.Parcela;
import br.com.fiadoFacil.dto.response.ParcelaEmAtrasoResponse;

@Component
public class RelatorioMapper {

    /** Espera a parcela com pagamento, venda e cliente já carregados. */
    public ParcelaEmAtrasoResponse toParcelaEmAtrasoResponse(Parcela parcela, LocalDate hoje) {
        Pagamento pagamento = parcela.getPagamento();
        Cliente cliente = pagamento.getVenda().getCliente();

        return ParcelaEmAtrasoResponse.builder()
                .parcelaId(parcela.getId())
                .vendaId(pagamento.getVenda().getId())
                .clienteId(cliente.getId())
                .clienteNome(cliente.getNome())
                .clienteTelefone(cliente.getTelefone())
                .numero(parcela.getNumero())
                .quantidadeParcelas(pagamento.getQuantidadeParcelas())
                .valor(parcela.getValor())
                .dataVencimento(parcela.getDataVencimento())
                .diasEmAtraso(ChronoUnit.DAYS.between(parcela.getDataVencimento(), hoje))
                .build();
    }
}
