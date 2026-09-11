package br.com.fiadoFacil.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.fiadoFacil.domain.Cliente;
import br.com.fiadoFacil.domain.Usuario;
import br.com.fiadoFacil.domain.enums.StatusParcela;
import br.com.fiadoFacil.dto.request.ClienteRequest;
import br.com.fiadoFacil.dto.response.ClienteResponse;
import br.com.fiadoFacil.dto.response.ClienteResumoFinanceiroResponse;
import br.com.fiadoFacil.mapper.ClienteMapper;
import br.com.fiadoFacil.repository.ClienteRepository;
import br.com.fiadoFacil.repository.VendaRepository;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final VendaRepository vendaRepository;
    private final ClienteMapper clienteMapper;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public ClienteService(ClienteRepository clienteRepository,
                          VendaRepository vendaRepository,
                          ClienteMapper clienteMapper,
                          UsuarioAutenticadoService usuarioAutenticadoService) {
        this.clienteRepository = clienteRepository;
        this.vendaRepository = vendaRepository;
        this.clienteMapper = clienteMapper;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @Transactional
    public ClienteResponse cadastrar(ClienteRequest request) {
        normalizar(request);
        Usuario usuarioLogado = usuarioAutenticadoService.get();

        validarDuplicidade(usuarioLogado.getId(), request, null);

        Cliente cliente = clienteMapper.toEntity(request, usuarioLogado);
        Cliente salvo = salvarComProtecaoDeCorrida(cliente);

        return clienteMapper.toResponse(salvo);
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar(String busca) {
        Usuario usuarioLogado = usuarioAutenticadoService.get();

        Map<Long, ClienteResumoFinanceiroResponse> resumoPorCliente = vendaRepository
                .buscarResumoFinanceiroPorUsuario(usuarioLogado.getId(), StatusParcela.EM_ABERTO)
                .stream()
                .collect(Collectors.toMap(ClienteResumoFinanceiroResponse::getClienteId, Function.identity()));

        return clienteRepository.buscarPorUsuario(usuarioLogado.getId(), busca)
                .stream()
                .map(cliente -> clienteMapper.toResponse(cliente, resumoPorCliente.get(cliente.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(Long id) {
        return montarRespostaComResumo(buscarClienteDoUsuarioLogado(id));
    }

    @Transactional
    public ClienteResponse editar(Long id, ClienteRequest request) {
        normalizar(request);
        Cliente cliente = buscarClienteDoUsuarioLogado(id);

        validarDuplicidade(cliente.getUsuario().getId(), request, id);

        clienteMapper.atualizarEntity(cliente, request);
        Cliente salvo = salvarComProtecaoDeCorrida(cliente);

        return montarRespostaComResumo(salvo);
    }

    @Transactional
    public void excluir(Long id) {
        Cliente cliente = buscarClienteDoUsuarioLogado(id);
        try {
            clienteRepository.delete(cliente);
            clienteRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Não é possível excluir um cliente que já possui vendas registradas.");
        }
    }

    // Um cliente recém-cadastrado ainda não tem vendas, então cadastrar()
    // usa o mapper direto (total zerado). Nos demais casos o resumo
    // financeiro precisa vir do banco para a resposta não mentir o saldo.
    private ClienteResponse montarRespostaComResumo(Cliente cliente) {
        Usuario usuarioLogado = usuarioAutenticadoService.get();

        ClienteResumoFinanceiroResponse resumo = vendaRepository
                .buscarResumoFinanceiroPorCliente(cliente.getId(), usuarioLogado.getId(), StatusParcela.EM_ABERTO)
                .orElse(null);

        return clienteMapper.toResponse(cliente, resumo);
    }

    private Cliente buscarClienteDoUsuarioLogado(Long id) {
        Usuario usuarioLogado = usuarioAutenticadoService.get();
        return clienteRepository.findByIdAndUsuarioId(id, usuarioLogado.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado."));
    }

    private void validarDuplicidade(Long usuarioId, ClienteRequest request, Long idAtual) {
        String cpf = request.getCpf();
        String telefone = request.getTelefone();

        if (cpf != null) {
            boolean duplicado = (idAtual == null)
                    ? clienteRepository.existsByUsuarioIdAndCpf(usuarioId, cpf)
                    : clienteRepository.existsByUsuarioIdAndCpfAndIdNot(usuarioId, cpf, idAtual);
            if (duplicado) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Já existe um cliente cadastrado com este CPF. " +
                                "Confira o número digitado ou procure o cliente na sua lista.");
            }
        }

        if (telefone != null) {
            boolean duplicado = (idAtual == null)
                    ? clienteRepository.existsByUsuarioIdAndTelefone(usuarioId, telefone)
                    : clienteRepository.existsByUsuarioIdAndTelefoneAndIdNot(usuarioId, telefone, idAtual);
            if (duplicado) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Já existe um cliente cadastrado com este telefone. " +
                                "Confira o número digitado ou procure o cliente na sua lista.");
            }
        }
    }

    private void normalizar(ClienteRequest request) {
        request.setCpf(vazioParaNulo(request.getCpf()));
        request.setTelefone(vazioParaNulo(request.getTelefone()));
    }

    private String vazioParaNulo(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

    private Cliente salvarComProtecaoDeCorrida(Cliente cliente) {
        try {
            Cliente salvo = clienteRepository.save(cliente);
            clienteRepository.flush();
            return salvo;
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Já existe um cliente cadastrado com este CPF ou telefone. " +
                            "Confira os dados digitados ou procure o cliente na sua lista.");
        }
    }
}
