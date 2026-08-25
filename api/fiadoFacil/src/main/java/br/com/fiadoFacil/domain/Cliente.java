package br.com.fiadoFacil.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cliente", uniqueConstraints = {
        @UniqueConstraint(name = "uq_cliente_usuario_cpf", columnNames = {"fk_usuario", "cpf"}),
        @UniqueConstraint(name = "uq_cliente_usuario_telefone", columnNames = {"fk_usuario", "telefone"})
})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_usuario", nullable = false)
    private Usuario usuario;

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String nome;

    @Size(max = 20)
    @Column(length = 20)
    private String telefone;

    @Size(max = 14)
    @Column(length = 14)
    private String cpf;

    @Size(max = 255)
    @Column(length = 255)
    private String endereco;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;
}