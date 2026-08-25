package br.com.fiadoFacil.domain;

import java.math.BigDecimal;

import br.com.fiadoFacil.domain.enums.FormaPagamento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pagamento")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_venda", nullable = false, unique = true)
    private Venda venda;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false, length = 20)
    private FormaPagamento formaPagamento;

    @NotNull
    @Min(1)
    @Column(name = "quantidade_parcelas", nullable = false)
    private Integer quantidadeParcelas;

    @NotNull
    @DecimalMin(value = "0.0")
    @Column(name = "juros_mes", nullable = false, precision = 5, scale = 2)
    private BigDecimal jurosMes;

    @NotNull
    @DecimalMin(value = "0.0")
    @Column(name = "valor_entrada", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorEntrada;
}