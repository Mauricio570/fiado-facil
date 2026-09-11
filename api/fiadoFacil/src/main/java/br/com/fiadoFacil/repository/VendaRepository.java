package br.com.fiadoFacil.repository;

import java.util.List;
import java.util.Optional;

import br.com.fiadoFacil.domain.enums.StatusParcela;
import br.com.fiadoFacil.domain.enums.StatusVenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.fiadoFacil.domain.Venda;
import br.com.fiadoFacil.dto.response.ClienteResumoFinanceiroResponse;

public interface VendaRepository extends JpaRepository<Venda, Long> {

    Optional<Venda> findByIdAndCliente_Usuario_Id(Long id, Long usuarioId);

    List<Venda> findAllByCliente_IdAndCliente_Usuario_IdOrderByDataCriacaoDesc(Long clienteId, Long usuarioId);

    @Query("SELECT COUNT(DISTINCT v.cliente.id) FROM Venda v " +
            "WHERE v.status = :status AND v.cliente.usuario.id = :usuarioId")
    long contarClientesComVendaPorStatus(@Param("usuarioId") Long usuarioId, @Param("status") StatusVenda status);

    @Query("SELECT new br.com.fiadoFacil.dto.response.ClienteResumoFinanceiroResponse(" +
            "c.id, " +
            "COALESCE(SUM(CASE WHEN p.status = :statusParcela THEN p.valor ELSE 0bd END), 0bd), " +
            "MAX(v.dataCriacao)) " +
            "FROM Venda v " +
            "JOIN v.cliente c " +
            "LEFT JOIN Pagamento pg ON pg.venda = v " +
            "LEFT JOIN Parcela p ON p.pagamento = pg " +
            "WHERE c.usuario.id = :usuarioId " +
            "GROUP BY c.id")
    List<ClienteResumoFinanceiroResponse> buscarResumoFinanceiroPorUsuario(
            @Param("usuarioId") Long usuarioId,
            @Param("statusParcela") StatusParcela statusParcela);

    @Query("SELECT new br.com.fiadoFacil.dto.response.ClienteResumoFinanceiroResponse(" +
            "c.id, " +
            "COALESCE(SUM(CASE WHEN p.status = :statusParcela THEN p.valor ELSE 0bd END), 0bd), " +
            "MAX(v.dataCriacao)) " +
            "FROM Venda v " +
            "JOIN v.cliente c " +
            "LEFT JOIN Pagamento pg ON pg.venda = v " +
            "LEFT JOIN Parcela p ON p.pagamento = pg " +
            "WHERE c.usuario.id = :usuarioId AND c.id = :clienteId " +
            "GROUP BY c.id")
    Optional<ClienteResumoFinanceiroResponse> buscarResumoFinanceiroPorCliente(
            @Param("clienteId") Long clienteId,
            @Param("usuarioId") Long usuarioId,
            @Param("statusParcela") StatusParcela statusParcela);
}
