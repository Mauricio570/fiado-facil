package br.com.fiadoFacil.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioCadastroRequest {

    @NotBlank
    @Size(max = 150)
    private String nomeEmpresa;

    @NotBlank
    @Size(max = 18)
    private String cnpj;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @NotBlank
    @Size(min = 8, max = 100)
    private String senha;

    @NotBlank
    private String repetirSenha;
}
