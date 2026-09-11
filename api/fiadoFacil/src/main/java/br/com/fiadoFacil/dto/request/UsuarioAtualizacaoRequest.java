package br.com.fiadoFacil.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioAtualizacaoRequest {

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

    /**
     * A troca de senha é opcional: os três campos abaixo só são exigidos
     * quando o usuário preenche uma nova senha. A senha atual é obrigatória
     * nesse caso para que quem encontrar uma sessão aberta não consiga trocar
     * a senha do dono da conta.
     */
    private String senhaAtual;

    @Size(min = 8, max = 100)
    private String novaSenha;

    private String repetirNovaSenha;
}
