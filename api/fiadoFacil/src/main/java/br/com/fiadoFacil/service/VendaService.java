package br.com.fiadoFacil.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import br.com.fiadoFacil.dto.response.VendaResumoFinanceiroResponse;
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

        Venda venda = Venda.builder()
                .cliente(cliente)
                .status(StatusVenda.EM_ABERTO)
                .build();
        venda = vendaRepository.save(venda);

        List<ItemVenda> itens = salvarItens(venda, request.getItens());
        BigDecimal valorItens = somarItens(itens);

        PagamentoCalculado calculado = processarPagamento(venda, request.getPagamento(), valorItens);
        List<Parcela> parcelas = persistirPagamento(calculado);

        vendaRepository.save(venda);

        return vendaMapper.toResponse(venda, itens, calculado.pagamento(), parcelas);
    }

    @Transactional
    public VendaResponse editar(Long id, VendaRequest request) {
        Usuario usuarioLogado = usuarioAutenticadoService.get();

        Venda venda = vendaRepository.findByIdAndCliente_Usuario_Id(id, usuarioLogado.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venda não encontrada."));

        validarEdicaoOuExclusaoPermitida(venda);
        apagarItensPagamentoEParcelas(venda);

        List<ItemVenda> itens = salvarItens(venda, request.getItens());
        BigDecimal valorItens = somarItens(itens);

        venda.setStatus(StatusVenda.EM_ABERTO);

        PagamentoCalculado calculado = processarPagamento(venda, request.getPagamento(), valorItens);
        List<Parcela> parcelas = persistirPagamento(calculado);

        vendaRepository.save(venda);

        return vendaMapper.toResponse(venda, itens, calculado.pagamento(), parcelas);
    }

    @Transactional
    public void excluir(Long id) {
        Usuario usuarioLogado = usuarioAutenticadoService.get();

        Venda venda = vendaRepository.findByIdAndCliente_Usuario_Id(id, usuarioLogado.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venda não encontrada."));

        validarEdicaoOuExclusaoPermitida(venda);
        apagarItensPagamentoEParcelas(venda);

        vendaRepository.delete(venda);
    }

    @Transactional(readOnly = true)
    public List<VendaResumoResponse> listarPorCliente(Long clienteId) {
        Usuario usuarioLogado = usuarioAutenticadoService.get();

        clienteRepository.findByIdAndUsuarioId(clienteId, usuarioLogado.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado."));

        // Saldo devedor, parcelas pagas e entrada de cada venda, buscados de
        // uma vez só para não repetir a consulta a cada linha do histórico.
        Map<Long, VendaResumoFinanceiroResponse> resumoPorVenda = pagamentoRepository
                .buscarResumoPorVendaDoCliente(clienteId, StatusParcela.EM_ABERTO, StatusParcela.PAGO)
                .stream()
                .collect(Collectors.toMap(VendaResumoFinanceiroResponse::getVendaId, Function.identity()));

        return vendaRepository
                .findAllByCliente_IdAndCliente_Usuario_IdOrderByDataCriacaoDesc(clienteId, usuarioLogado.getId())
                .stream()
                .map(venda -> {
                    VendaResumoFinanceiroResponse resumo = resumoPorVenda.get(venda.getId());

                    BigDecimal valorItens = somarItens(itemVendaRepository.findAllByVendaId(venda.getId()));

                    return vendaMapper.toResumoResponse(
                            venda,
                            valorItens,
                            resumo == null ? valorItens : resumo.getValorTotalComJuros(),
                            resumo == null ? BigDecimal.ZERO : resumo.getTotalEmAberto(),
                            podeAlterar(venda, resumo));
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

    // Uma venda só pode ser alterada ou excluída enquanto o cliente
    // não pagou nada por ela. Bloqueia em três situações: venda já
    // quitada (cobre a venda à vista, que nasce paga), qualquer
    // parcela já paga, e entrada recebida — o dinheiro da entrada já
    // está no caixa e editar a venda apagaria esse registro.
    private void validarEdicaoOuExclusaoPermitida(Venda venda) {
        if (venda.getStatus() == StatusVenda.PAGO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Não é possível alterar ou excluir uma venda já quitada.");
        }

        Pagamento pagamento = pagamentoRepository.findByVendaId(venda.getId()).orElse(null);
        if (pagamento == null) {
            return;
        }

        boolean temParcelaPaga = parcelaRepository.findAllByPagamentoIdOrderByNumeroAsc(pagamento.getId())
                .stream()
                .anyMatch(p -> p.getStatus() == StatusParcela.PAGO);
        if (temParcelaPaga) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Não é possível alterar ou excluir uma venda que já teve parcela paga.");
        }

        if (temEntradaPaga(pagamento.getValorEntrada())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Não é possível alterar ou excluir uma venda que já teve entrada paga.");
        }
    }

    private boolean temEntradaPaga(BigDecimal valorEntrada) {
        return valorEntrada != null && valorEntrada.compareTo(BigDecimal.ZERO) > 0;
    }

    // Mesma regra de validarEdicaoOuExclusaoPermitida, porém em forma
    // de predicado, para a listagem informar à tela quais botões de
    // editar/excluir devem ficar habilitados.
    private boolean podeAlterar(Venda venda, VendaResumoFinanceiroResponse resumo) {
        if (venda.getStatus() == StatusVenda.PAGO || resumo == null) {
            return false;
        }

        return resumo.getParcelasPagas() == 0 && !temEntradaPaga(resumo.getValorEntrada());
    }

    // Apaga item_venda, parcela e pagamento EXPLICITAMENTE pelo
    // Hibernate — não dá pra confiar só no ON DELETE CASCADE do
    // banco aqui, porque validarEdicaoOuExclusaoPermitida() já
    // carregou pagamento/parcelas na memória da sessão do Hibernate.
    // Se a gente deixasse o banco apagar por trás, o Hibernate ainda
    // acha que esses objetos "vivem", e reclama de inconsistência na
    // hora do commit. Usado tanto em editar() quanto em excluir().
    private void apagarItensPagamentoEParcelas(Venda venda) {
        itemVendaRepository.deleteAll(itemVendaRepository.findAllByVendaId(venda.getId()));

        Pagamento pagamentoAntigo = pagamentoRepository.findByVendaId(venda.getId()).orElse(null);
        if (pagamentoAntigo != null) {
            parcelaRepository.deleteAll(parcelaRepository.findAllByPagamentoIdOrderByNumeroAsc(pagamentoAntigo.getId()));
            pagamentoRepository.delete(pagamentoAntigo);
        }
        pagamentoRepository.flush();
    }

    private List<ItemVenda> salvarItens(Venda venda, List<ItemVendaRequest> itensRequest) {
        List<ItemVenda> itens = new ArrayList<>();
        for (ItemVendaRequest itemRequest : itensRequest) {
            itens.add(itemVendaRepository.save(vendaMapper.toEntity(itemRequest, venda)));
        }
        return itens;
    }

    private BigDecimal somarItens(List<ItemVenda> itens) {
        return itens.stream()
                .map(ItemVenda::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Calcula (sem persistir ainda) o Pagamento e as Parcelas com
    // base na forma de pagamento escolhida — usado tanto no cadastro
    // quanto na edição, pra não duplicar a regra de juros em dois
    // lugares.
    private PagamentoCalculado processarPagamento(Venda venda, PagamentoRequest pagamentoRequest, BigDecimal valorItens) {
        if (pagamentoRequest.getFormaPagamento() == FormaPagamento.A_VISTA) {
            Pagamento pagamento = Pagamento.builder()
                    .venda(venda)
                    .formaPagamento(FormaPagamento.A_VISTA)
                    .quantidadeParcelas(1)
                    .jurosMes(BigDecimal.ZERO)
                    .valorEntrada(valorItens)
                    .build();
            venda.setStatus(StatusVenda.PAGO);

            return new PagamentoCalculado(pagamento, List.of());
        }

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

        Pagamento pagamento = Pagamento.builder()
                .venda(venda)
                .formaPagamento(FormaPagamento.CREDITO)
                .quantidadeParcelas(quantidadeParcelas)
                .jurosMes(jurosMes)
                .valorEntrada(valorEntrada)
                .build();

        List<Parcela> parcelas = gerarParcelas(valorFinal, quantidadeParcelas, dataDaVenda(venda));

        return new PagamentoCalculado(pagamento, parcelas);
    }

    private List<Parcela> persistirPagamento(PagamentoCalculado calculado) {
        Pagamento pagamentoSalvo = pagamentoRepository.save(calculado.pagamento());

        List<Parcela> parcelasSalvas = new ArrayList<>();
        for (Parcela parcela : calculado.parcelas()) {
            parcela.setPagamento(pagamentoSalvo);
            parcelasSalvas.add(parcelaRepository.save(parcela));
        }
        return parcelasSalvas;
    }

    // Data base do parcelamento. A venda já foi salva quando chegamos aqui,
    // então o @CreationTimestamp já preencheu dataCriacao; o fallback cobre
    // só o caso de a venda ainda não ter sido persistida.
    private LocalDate dataDaVenda(Venda venda) {
        return venda.getDataCriacao() != null ? venda.getDataCriacao().toLocalDate() : LocalDate.now();
    }

    // Divide valorFinal em N parcelas iguais; a última absorve a
    // diferença de arredondamento, pra soma das parcelas bater
    // exatamente com valorFinal. Cada parcela vence N meses após a venda —
    // compra em 15/07 parcelada em 3x vence em 15/08, 15/09 e 15/10.
    // O plusMonths já ajusta o dia quando o mês de destino é mais curto
    // (31/01 + 1 mês = 28/02), então não existe data inválida.
    private List<Parcela> gerarParcelas(BigDecimal valorFinal, int quantidade, LocalDate dataDaVenda) {
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
                    .dataVencimento(dataDaVenda.plusMonths(numero))
                    .status(StatusParcela.EM_ABERTO)
                    .build());
        }

        return parcelas;
    }

    private record PagamentoCalculado(Pagamento pagamento, List<Parcela> parcelas) {
    }
}