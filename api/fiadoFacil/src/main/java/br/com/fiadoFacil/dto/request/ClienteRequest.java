package br.com.fiadoFacil.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClienteRequest {

    @NotBlank
    @Size(max = 150)
    private String nome;

    @Size(max = 20)
    private String telefone;

    @Size(max = 14)
    private String cpf;

    @Size(max = 255)
    private String endereco;
}