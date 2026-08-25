package br.com.fiadoFacil.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class UsuarioResponse {

    private Long id;
    private String nomeEmpresa;
    private String cnpj;
    private String email;
    private LocalDateTime dataCriacao;
}
