package br.com.fiadoFacil.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import br.com.fiadoFacil.domain.enums.FormaPagamento;
import br.com.fiadoFacil.domain.enums.StatusParcela;
import br.com.fiadoFacil.domain.enums.StatusVenda;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.fiadoFacil.domain.Cliente;
import br.com.fiadoFacil.domain.ItemVenda;
import br.com.fiadoFacil.domain.Pagamento;
import br.com.fiadoFacil.domain.Parcela;
import br.com.fiadoFacil.domain.Usuario;
import br.com.fiadoFacil.domain.Venda;
import br.com.fiadoFacil.dto.request.ItemVendaRequest;
import br.com.fiadoFacil.dto.request.PagamentoRequest;
import br.com.fiadoFacil.dto.request.VendaRequest;
import br.com.fiadoFacil.dto.response.VendaResponse;
import br.com.fiadoFacil.dto.response.VendaResumoResponse;
import br.com.fiadoFacil.mapper.VendaMapper;
import br.com.fiadoFacil.repository.ClienteRepository;
import br.com.fiadoFacil.repository.ItemVendaRepository;
import br.com.fiadoFacil.repository.PagamentoRepository;
import br.com.fiadoFacil.repository.ParcelaRepository;
import br.com.fiadoFacil.repository.VendaRepository;

@Service
public class VendaService {

    private final VendaRepository vendaRepository;
    private final ItemVendaRepository itemVendaRepository;
    private final PagamentoRepository pagamentoRepository;
    private final ParcelaRepository parcelaRepository;
    private final ClienteRepository clienteRepository;
    private final VendaMapper vendaMapper;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public VendaService(VendaRepository vendaRepository,
                        ItemVendaRepository itemVendaRepository,
                        PagamentoRepository pagamentoRepository,
                        ParcelaRepository parcelaRepository,
                        ClienteRepository clienteRepository,
                        VendaMapper vendaMapper,
                        UsuarioAutenticadoService usuarioAutenticadoService) {
        this.vendaRepository = vendaRepository;
        this.itemVendaRepository = itemVendaRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.parcelaRepository = parcelaRepository;
        this.clienteRepository = clienteRepository;
        this.vendaMapper = vendaMapper;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @Transactional
    public VendaResponse cadastrar(VendaRequest request) {
        Usuario usuarioLogado = usuarioAutenticadoService.get();

        Cliente cliente = clienteRepository.findByIdAndUsuarioId(request.getFkCliente(), usuarioLogado.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado."));

        BigDecimal valorItens = BigDecimal.ZERO;
        for (ItemVendaRequest itemRequest : request.getItens()) {
            valorItens = valorItens.add(
                    itemRequest.getValorUnitario().multiply(BigDecimal.valueOf(itemRequest.getQuantidade())));
        }

        Venda venda = Venda.builder()
                .cliente(cliente)
                .status(StatusVenda.EM_ABERTO)
                .build();
        venda = vendaRepository.save(venda);

        List<ItemVenda> itens = new ArrayList<>();
        for (ItemVendaRequest itemRequest : request.getItens()) {
            itens.add(itemVendaRepository.save(vendaMapper.toEntity(itemRequest, venda)));
        }

        PagamentoRequest pagamentoRequest = request.getPagamento();
        Pagamento pagamento;
        List<Parcela> parcelas = new ArrayList<>();

        if (pagamentoRequest.getFormaPagamento() == FormaPagamento.A_VISTA) {
            pagamento = Pagamento.builder()
                    .venda(venda)
                    .formaPagamento(FormaPagamento.A_VISTA)
                    .quantidadeParcelas(1)
                    .jurosMes(BigDecimal.ZERO)
                    .valorEntrada(valorItens)
                    .build();
            venda.setStatus(StatusVenda.PAGO);

        } else {
            Integer quantidadeParcelas = pagamentoRequest.getQuantidadeParcelas();
            if (quantidadeParcelas == null || quantidadeParcelas < 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Informe a quantidade de parcelas para pagamento parcelado.");
            }

            BigDecimal valorEntrada = pagamentoRequest.getValorEntrada() != null
                    ? pagamentoRequest.getValorEntrada() : BigDecimal.ZERO;
            BigDecimal jurosMes = pagamentoRequest.getJurosMes() != null
                    ? pagamentoRequest.getJurosMes() : BigDecimal.ZERO;

            BigDecimal valorFinanciado = valorItens.subtract(valorEntrada);
            if (valorFinanciado.compareTo(BigDecimal.ZERO) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "O valor de entrada não pode ser maior que o total da compra.");
            }

            BigDecimal taxaTotal = jurosMes.divide(BigDecimal.valueOf(100))
                    .multiply(BigDecimal.valueOf(quantidadeParcelas));
            BigDecimal jurosTotal = valorFinanciado.multiply(taxaTotal);
            BigDecimal valorFinal = valorFinanciado.add(jurosTotal).setScale(2, RoundingMode.HALF_UP);

            pagamento = Pagamento.builder()
                    .venda(venda)
                    .formaPagamento(FormaPagamento.CREDITO)
                    .quantidadeParcelas(quantidadeParcelas)
                    .jurosMes(jurosMes)
                    .valorEntrada(valorEntrada)
                    .build();

            parcelas = gerarParcelas(valorFinal, quantidadeParcelas);
        }

        pagamento = pagamentoRepository.save(pagamento);
        vendaRepository.save(venda);

        for (Parcela parcela : parcelas) {
            parcela.setPagamento(pagamento);
            parcelaRepository.save(parcela);
        }

        return vendaMapper.toResponse(venda, itens, pagamento, parcelas);
    }

    @Transactional(readOnly = true)
    public List<VendaResumoResponse> listarPorCliente(Long clienteId) {
        Usuario usuarioLogado = usuarioAutenticadoService.get();

        clienteRepository.findByIdAndUsuarioId(clienteId, usuarioLogado.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado."));

        return vendaRepository
                .findAllByCliente_IdAndCliente_Usuario_IdOrderByDataCriacaoDesc(clienteId, usuarioLogado.getId())
                .stream()
                .map(venda -> {
                    BigDecimal total = itemVendaRepository.findAllByVendaId(venda.getId()).stream()
                            .map(ItemVenda::getValorTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return vendaMapper.toResumoResponse(venda, total);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public VendaResponse buscarDetalhe(Long id) {
        Usuario usuarioLogado = usuarioAutenticadoService.get();

        Venda venda = vendaRepository.findByIdAndCliente_Usuario_Id(id, usuarioLogado.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venda não encontrada."));

        List<ItemVenda> itens = itemVendaRepository.findAllByVendaId(venda.getId());
        Pagamento pagamento = pagamentoRepository.findByVendaId(venda.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Venda sem pagamento associado — estado inconsistente."));
        List<Parcela> parcelas = parcelaRepository.findAllByPagamentoIdOrderByNumeroAsc(pagamento.getId());

        return vendaMapper.toResponse(venda, itens, pagamento, parcelas);
    }

    private List<Parcela> gerarParcelas(BigDecimal valorFinal, int quantidade) {
        List<Parcela> parcelas = new ArrayList<>();
        BigDecimal valorParcela = valorFinal.divide(BigDecimal.valueOf(quantidade), 2, RoundingMode.DOWN);
        BigDecimal somaParcial = BigDecimal.ZERO;

        for (int numero = 1; numero <= quantidade; numero++) {
            BigDecimal valor;
            if (numero < quantidade) {
                valor = valorParcela;
                somaParcial = somaParcial.add(valor);
            } else {
                valor = valorFinal.subtract(somaParcial);
            }

            parcelas.add(Parcela.builder()
                    .numero(numero)
                    .valor(valor)
                    .status(StatusParcela.EM_ABERTO)
                    .build());
        }

        return parcelas;
    }
}