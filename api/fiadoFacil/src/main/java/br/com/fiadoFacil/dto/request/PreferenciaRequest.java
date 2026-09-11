package br.com.fiadoFacil.dto.request;

import br.com.fiadoFacil.domain.enums.TamanhoTexto;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PreferenciaRequest {

    @NotNull
    private TamanhoTexto tamanhoTexto;
}
