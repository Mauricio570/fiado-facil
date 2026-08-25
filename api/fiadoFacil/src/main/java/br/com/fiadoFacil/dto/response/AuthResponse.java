package br.com.fiadoFacil.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String tipo;
    private long expiraEmSegundos;
}