package br.com.fiadoFacil.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemVendaRequest {

    @NotBlank
    @Size(max = 150)
    private String nome;

    @NotNull
    @Min(1)
    private Integer quantidade;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal valorUnitario;

}