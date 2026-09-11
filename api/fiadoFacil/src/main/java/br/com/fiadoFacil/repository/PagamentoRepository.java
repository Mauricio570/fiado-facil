package br.com.fiadoFacil.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.fiadoFacil.domain.Pagamento;
import br.com.fiadoFacil.domain.enums.StatusParcela;
import br.com.fiadoFacil.dto.response.VendaResumoFinanceiroResponse;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    Optional<Pagamento> findByVendaId(Long vendaId);

    @Query("SELECT COALESCE(SUM(pg.valorEntrada), 0) FROM Pagamento pg " +
            "WHERE pg.venda.dataCriacao BETWEEN :inicio AND :fim " +
            "AND pg.venda.cliente.usuario.id = :usuarioId")
    BigDecimal somarEntradasNoPeriodo(@Param("usuarioId") Long usuarioId,
                                      @Param("inicio") LocalDateTime inicio,
                                      @Param("fim") LocalDateTime fim);

    /**
     * Reúne, em uma consulta só, tudo que o histórico de compras precisa saber
     * sobre cada venda do cliente: saldo devedor, quantas parcelas já foram
     * pagas e quanto foi pago de entrada.
     */
    @Query("SELECT new br.com.fiadoFacil.dto.response.VendaResumoFinanceiroResponse(" +
            "v.id, " +
            "COALESCE(SUM(CASE WHEN p.status = :statusEmAberto THEN p.valor ELSE 0bd END), 0bd), " +
            "COALESCE(SUM(p.valor), 0bd), " +
            "COUNT(CASE WHEN p.status = :statusPago THEN p.id ELSE NULL END), " +
            "pg.valorEntrada) " +
            "FROM Pagamento pg " +
            "JOIN pg.venda v " +
            "LEFT JOIN Parcela p ON p.pagamento = pg " +
            "WHERE v.cliente.id = :clienteId " +
            "GROUP BY v.id, pg.valorEntrada")
    List<VendaResumoFinanceiroResponse> buscarResumoPorVendaDoCliente(
            @Param("clienteId") Long clienteId,
            @Param("statusEmAberto") StatusParcela statusEmAberto,
            @Param("statusPago") StatusParcela statusPago);
}