package br.com.fiadoFacil.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ClienteResponse {

    private Long id;
    private String nome;
    private String telefone;
    private String cpf;
    private String endereco;
    private LocalDateTime dataCriacao;
    private BigDecimal totalEmAberto;
    private LocalDateTime ultimaCompra;

}
