package br.com.fiadoFacil.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClienteEmDebitoResponse {

    private Long clienteId;
    private String nome;
    private BigDecimal totalEmAberto;
}