package br.com.fiadoFacil.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import br.com.fiadoFacil.domain.enums.StatusParcela;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.fiadoFacil.domain.Parcela;
import br.com.fiadoFacil.dto.response.ClienteEmDebitoResponse;

public interface ParcelaRepository extends JpaRepository<Parcela, Long> {

    List<Parcela> findAllByPagamentoIdOrderByNumeroAsc(Long pagamentoId);

    Optional<Parcela> findByIdAndPagamento_Venda_Cliente_Usuario_Id(Long id, Long usuarioId);

    @Query("SELECT COALESCE(SUM(p.valor), 0) FROM Parcela p " +
            "WHERE p.status = :status AND p.pagamento.venda.cliente.usuario.id = :usuarioId")
    BigDecimal somarPorStatus(@Param("usuarioId") Long usuarioId, @Param("status") StatusParcela status);

    @Query("SELECT COALESCE(SUM(p.valor), 0) FROM Parcela p " +
            "WHERE p.status = :status AND p.dataPagamento BETWEEN :inicio AND :fim " +
            "AND p.pagamento.venda.cliente.usuario.id = :usuarioId")
    BigDecimal somarPorStatusEPeriodo(@Param("usuarioId") Long usuarioId,
                                      @Param("status") StatusParcela status,
                                      @Param("inicio") LocalDate inicio,
                                      @Param("fim") LocalDate fim);

    @Query("SELECT new br.com.fiadoFacil.dto.response.ClienteEmDebitoResponse(c.id, c.nome, SUM(p.valor)) " +
            "FROM Parcela p " +
            "JOIN p.pagamento pg " +
            "JOIN pg.venda v " +
            "JOIN v.cliente c " +
            "WHERE p.status = :status AND c.usuario.id = :usuarioId " +
            "GROUP BY c.id, c.nome " +
            "ORDER BY SUM(p.valor) DESC")
    List<ClienteEmDebitoResponse> buscarClientesComContasEmAberto(@Param("usuarioId") Long usuarioId,
                                                                  @Param("status") StatusParcela status);
}