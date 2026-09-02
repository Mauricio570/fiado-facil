package br.com.fiadoFacil.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

import br.com.fiadoFacil.domain.enums.StatusParcela;
import br.com.fiadoFacil.domain.enums.StatusVenda;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiadoFacil.domain.Usuario;
import br.com.fiadoFacil.dto.response.ClienteEmDebitoResponse;
import br.com.fiadoFacil.dto.response.PainelResponse;
import br.com.fiadoFacil.repository.ClienteRepository;
import br.com.fiadoFacil.repository.PagamentoRepository;
import br.com.fiadoFacil.repository.ParcelaRepository;
import br.com.fiadoFacil.repository.VendaRepository;

@Service
public class PainelService {

    private final ClienteRepository clienteRepository;
    private final VendaRepository vendaRepository;
    private final ParcelaRepository parcelaRepository;
    private final PagamentoRepository pagamentoRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public PainelService(ClienteRepository clienteRepository,
                         VendaRepository vendaRepository,
                         ParcelaRepository parcelaRepository,
                         PagamentoRepository pagamentoRepository,
                         UsuarioAutenticadoService usuarioAutenticadoService) {
        this.clienteRepository = clienteRepository;
        this.vendaRepository = vendaRepository;
        this.parcelaRepository = parcelaRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @Transactional(readOnly = true)
    public PainelResponse buscarResumo() {
        Usuario usuarioLogado = usuarioAutenticadoService.get();
        Long usuarioId = usuarioLogado.getId();

        YearMonth mesAtual = YearMonth.now();
        LocalDate inicioMes = mesAtual.atDay(1);
        LocalDate fimMes = mesAtual.atEndOfMonth();
        LocalDateTime inicioMesDateTime = inicioMes.atStartOfDay();
        LocalDateTime fimMesDateTime = fimMes.atTime(LocalTime.MAX);

        BigDecimal totalAReceber = parcelaRepository.somarPorStatus(usuarioId, StatusParcela.EM_ABERTO);

        BigDecimal entradasNoMes = pagamentoRepository.somarEntradasNoPeriodo(usuarioId, inicioMesDateTime, fimMesDateTime);
        BigDecimal parcelasPagasNoMes = parcelaRepository.somarPorStatusEPeriodo(
                usuarioId, StatusParcela.PAGO, inicioMes, fimMes);
        BigDecimal recebidoEsteMes = entradasNoMes.add(parcelasPagasNoMes);

        long totalClientes = clienteRepository.countByUsuarioId(usuarioId);
        long clientesEmDebito = vendaRepository.contarClientesComVendaPorStatus(usuarioId, StatusVenda.EM_ABERTO);

        List<ClienteEmDebitoResponse> clientesComContasEmAberto =
                parcelaRepository.buscarClientesComContasEmAberto(usuarioId, StatusParcela.EM_ABERTO);

        return PainelResponse.builder()
                .totalAReceber(totalAReceber)
                .recebidoEsteMes(recebidoEsteMes)
                .clientesEmDebito(clientesEmDebito)
                .totalClientes(totalClientes)
                .clientesComContasEmAberto(clientesComContasEmAberto)
                .build();
    }
}