package br.com.fiadoFacil.service;

import java.time.LocalDate;
import java.util.List;

import br.com.fiadoFacil.domain.enums.StatusParcela;
import br.com.fiadoFacil.domain.enums.StatusVenda;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.fiadoFacil.domain.Parcela;
import br.com.fiadoFacil.domain.Usuario;
import br.com.fiadoFacil.domain.Venda;
import br.com.fiadoFacil.dto.response.ParcelaResponse;
import br.com.fiadoFacil.mapper.VendaMapper;
import br.com.fiadoFacil.repository.ParcelaRepository;
import br.com.fiadoFacil.repository.VendaRepository;

@Service
public class ParcelaService {

    private final ParcelaRepository parcelaRepository;
    private final VendaRepository vendaRepository;
    private final VendaMapper vendaMapper;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public ParcelaService(ParcelaRepository parcelaRepository,
                          VendaRepository vendaRepository,
                          VendaMapper vendaMapper,
                          UsuarioAutenticadoService usuarioAutenticadoService) {
        this.parcelaRepository = parcelaRepository;
        this.vendaRepository = vendaRepository;
        this.vendaMapper = vendaMapper;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @Transactional
    public ParcelaResponse marcarComoPaga(Long id) {
        Usuario usuarioLogado = usuarioAutenticadoService.get();

        Parcela parcela = parcelaRepository
                .findByIdAndPagamento_Venda_Cliente_Usuario_Id(id, usuarioLogado.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parcela não encontrada."));

        if (parcela.getStatus() == StatusParcela.PAGO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta parcela já está paga.");
        }

        parcela.setStatus(StatusParcela.PAGO);
        parcela.setDataPagamento(LocalDate.now());
        parcelaRepository.save(parcela);

        atualizarStatusVendaSeQuitada(parcela);

        return vendaMapper.toParcelaResponse(parcela);
    }

    private void atualizarStatusVendaSeQuitada(Parcela parcela) {
        Long pagamentoId = parcela.getPagamento().getId();
        List<Parcela> todasParcelas = parcelaRepository.findAllByPagamentoIdOrderByNumeroAsc(pagamentoId);

        boolean todasPagas = todasParcelas.stream().allMatch(p -> p.getStatus() == StatusParcela.PAGO);

        if (todasPagas) {
            Venda venda = parcela.getPagamento().getVenda();
            venda.setStatus(StatusVenda.PAGO);
            vendaRepository.save(venda);
        }
    }
}