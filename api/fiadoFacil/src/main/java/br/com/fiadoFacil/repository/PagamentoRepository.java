package br.com.fiadoFacil.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.fiadoFacil.domain.Pagamento;
import br.com.fiadoFacil.domain.enums.StatusParcela;
import br.com.fiadoFacil.dto.response.RelatorioVendaResponse;
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

    /**
     * Vendas feitas no período, de todos os clientes do usuário, com as somas
     * das parcelas de cada uma (total, pagas e vencidas em aberto). Base do
     * histórico de vendas e dos gráficos mensais dos relatórios.
     */
    @Query("SELECT new br.com.fiadoFacil.dto.response.RelatorioVendaResponse(" +
            "v.id, v.dataCriacao, c.id, c.nome, v.status, " +
            "pg.formaPagamento, pg.quantidadeParcelas, pg.valorEntrada, " +
            "COALESCE(SUM(p.valor), 0bd), " +
            "COALESCE(SUM(CASE WHEN p.status = :statusPago THEN p.valor ELSE 0bd END), 0bd), " +
            "COALESCE(SUM(CASE WHEN p.status = :statusEmAberto AND p.dataVencimento < :hoje " +
            "THEN p.valor ELSE 0bd END), 0bd)) " +
            "FROM Pagamento pg " +
            "JOIN pg.venda v " +
            "JOIN v.cliente c " +
            "LEFT JOIN Parcela p ON p.pagamento = pg " +
            "WHERE c.usuario.id = :usuarioId AND v.dataCriacao BETWEEN :inicio AND :fim " +
            "GROUP BY v.id, v.dataCriacao, c.id, c.nome, v.status, " +
            "pg.formaPagamento, pg.quantidadeParcelas, pg.valorEntrada " +
            "ORDER BY v.dataCriacao DESC")
    List<RelatorioVendaResponse> buscarVendasDoPeriodo(@Param("usuarioId") Long usuarioId,
                                                       @Param("inicio") LocalDateTime inicio,
                                                       @Param("fim") LocalDateTime fim,
                                                       @Param("hoje") LocalDate hoje,
                                                       @Param("statusPago") StatusParcela statusPago,
                                                       @Param("statusEmAberto") StatusParcela statusEmAberto);
}