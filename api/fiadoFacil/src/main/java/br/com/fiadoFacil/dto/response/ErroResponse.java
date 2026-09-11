package br.com.fiadoFacil.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;

/**
 * Corpo padrão de todo erro devolvido pela API. O campo `mensagem` é escrito
 * para ser exibido diretamente ao usuário final — o front-end não precisa
 * traduzir código de status nem montar texto próprio.
 */
@Getter
@Builder
public class ErroResponse {

    private LocalDateTime momento;
    private int status;
    private String mensagem;

    /** Preenchido apenas em erro de validação: nome do campo e o que há de errado. */
    private Map<String, String> campos;
}
