package br.com.fiadoFacil.mapper;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;

import br.com.fiadoFacil.domain.Cliente;
import br.com.fiadoFacil.domain.ItemVenda;
import br.com.fiadoFacil.domain.Pagamento;
import br.com.fiadoFacil.domain.Parcela;
import br.com.fiadoFacil.domain.Venda;
import br.com.fiadoFacil.dto.request.ItemVendaRequest;
import br.com.fiadoFacil.dto.response.ItemVendaResponse;
import br.com.fiadoFacil.dto.response.PagamentoResponse;
import br.com.fiadoFacil.dto.response.ParcelaResponse;
import br.com.fiadoFacil.dto.response.VendaResponse;
import br.com.fiadoFacil.dto.response.VendaResumoResponse;

@Component
public class VendaMapper {

    public ItemVenda toEntity(ItemVendaRequest request, Venda venda) {
        BigDecimal valorTotal = request.getValorUnitario()
                .multiply(BigDecimal.valueOf(request.getQuantidade()));

        return ItemVenda.builder()
                .venda(venda)
                .nome(request.getNome())
                .quantidade(request.getQuantidade())
                .valorUnitario(request.getValorUnitario())
                .valorTotal(valorTotal)
                .build();
    }

    public VendaResponse toResponse(Venda venda, List<ItemVenda> itens, Pagamento pagamento, List<Parcela> parcelas) {
        Cliente cliente = venda.getCliente();

        BigDecimal valorTotal = itens.stream()
                .map(ItemVenda::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return VendaResponse.builder()
                .id(venda.getId())
                .clienteId(cliente.getId())
                .clienteNome(cliente.getNome())
                .status(venda.getStatus())
                .dataCriacao(venda.getDataCriacao())
                .valorTotal(valorTotal)
                .itens(itens.stream().map(this::toItemResponse).toList())
                .pagamento(toPagamentoResponse(pagamento, parcelas))
                .build();
    }

    public VendaResumoResponse toResumoResponse(Venda venda, BigDecimal valorTotal,
                                               BigDecimal valorTotalComJuros,
                                               BigDecimal totalEmAberto, boolean podeAlterar) {
        return VendaResumoResponse.builder()
                .id(venda.getId())
                .status(venda.getStatus())
                .dataCriacao(venda.getDataCriacao())
                .valorTotal(valorTotal)
                .valorTotalComJuros(valorTotalComJuros)
                .totalEmAberto(totalEmAberto)
                .podeAlterar(podeAlterar)
                .build();
    }

    private ItemVendaResponse toItemResponse(ItemVenda item) {
        return ItemVendaResponse.builder()
                .id(item.getId())
                .nome(item.getNome())
                .quantidade(item.getQuantidade())
                .valorUnitario(item.getValorUnitario())
                .valorTotal(item.getValorTotal())
                .build();
    }

    private PagamentoResponse toPagamentoResponse(Pagamento pagamento, List<Parcela> parcelas) {
        return PagamentoResponse.builder()
                .id(pagamento.getId())
                .formaPagamento(pagamento.getFormaPagamento())
                .quantidadeParcelas(pagamento.getQuantidadeParcelas())
                .jurosMes(pagamento.getJurosMes())
                .valorEntrada(pagamento.getValorEntrada())
                .parcelas(parcelas.stream().map(this::toParcelaResponse).toList())
                .build();
    }

    public ParcelaResponse toParcelaResponse(Parcela parcela) {
        return ParcelaResponse.builder()
                .id(parcela.getId())
                .numero(parcela.getNumero())
                .valor(parcela.getValor())
                .dataPagamento(parcela.getDataPagamento())
                .status(parcela.getStatus())
                .build();
    }
}