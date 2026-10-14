package br.com.fiadoFacil.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErroResponse {

    private LocalDateTime momento;
    private int status;
    private String mensagem;

    private Map<String, String> campos;
}
