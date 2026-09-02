package br.com.fiadoFacil.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.fiadoFacil.domain.Pagamento;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    Optional<Pagamento> findByVendaId(Long vendaId);

    @Query("SELECT COALESCE(SUM(pg.valorEntrada), 0) FROM Pagamento pg " +
            "WHERE pg.venda.dataCriacao BETWEEN :inicio AND :fim " +
            "AND pg.venda.cliente.usuario.id = :usuarioId")
    BigDecimal somarEntradasNoPeriodo(@Param("usuarioId") Long usuarioId,
                                      @Param("inicio") LocalDateTime inicio,
                                      @Param("fim") LocalDateTime fim);
}