package br.com.fiadoFacil.dto.response;

import br.com.fiadoFacil.domain.enums.TamanhoTexto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PreferenciaResponse {

    private TamanhoTexto tamanhoTexto;
}
