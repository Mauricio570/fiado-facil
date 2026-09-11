package br.com.fiadoFacil.mapper;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import br.com.fiadoFacil.domain.Cliente;
import br.com.fiadoFacil.domain.Usuario;
import br.com.fiadoFacil.dto.request.ClienteRequest;
import br.com.fiadoFacil.dto.response.ClienteResponse;
import br.com.fiadoFacil.dto.response.ClienteResumoFinanceiroResponse;

@Component
public class ClienteMapper {

    public Cliente toEntity(ClienteRequest request, Usuario usuario) {
        return Cliente.builder()
                .usuario(usuario)
                .nome(request.getNome())
                .telefone(request.getTelefone())
                .cpf(request.getCpf())
                .endereco(request.getEndereco())
                .build();
    }

    public void atualizarEntity(Cliente cliente, ClienteRequest request) {
        cliente.setNome(request.getNome());
        cliente.setTelefone(request.getTelefone());
        cliente.setCpf(request.getCpf());
        cliente.setEndereco(request.getEndereco());
    }

    public ClienteResponse toResponse(Cliente cliente) {
        return toResponse(cliente, null);
    }

    public ClienteResponse toResponse(Cliente cliente, ClienteResumoFinanceiroResponse resumo) {
        return ClienteResponse.builder()
                .id(cliente.getId())
                .nome(cliente.getNome())
                .telefone(cliente.getTelefone())
                .cpf(cliente.getCpf())
                .endereco(cliente.getEndereco())
                .dataCriacao(cliente.getDataCriacao())
                .totalEmAberto(resumo == null ? BigDecimal.ZERO : resumo.getTotalEmAberto())
                .ultimaCompra(resumo == null ? null : resumo.getUltimaCompra())
                .build();
    }
}
