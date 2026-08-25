package br.com.fiadoFacil.mapper;

import br.com.fiadoFacil.domain.Usuario;
import br.com.fiadoFacil.dto.response.UsuarioResponse;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public UsuarioResponse toResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nomeEmpresa(usuario.getNomeEmpresa())
                .cnpj(usuario.getCnpj())
                .email(usuario.getEmail())
                .dataCriacao(usuario.getDataCriacao())
                .build();
    }
}
