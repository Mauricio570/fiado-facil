package br.com.fiadoFacil.repository;

import java.util.List;
import java.util.Optional;

import br.com.fiadoFacil.domain.enums.StatusVenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.fiadoFacil.domain.Venda;

public interface VendaRepository extends JpaRepository<Venda, Long> {

    Optional<Venda> findByIdAndCliente_Usuario_Id(Long id, Long usuarioId);

    List<Venda> findAllByCliente_IdAndCliente_Usuario_IdOrderByDataCriacaoDesc(Long clienteId, Long usuarioId);

    @Query("SELECT COUNT(DISTINCT v.cliente.id) FROM Venda v " +
            "WHERE v.status = :status AND v.cliente.usuario.id = :usuarioId")
    long contarClientesComVendaPorStatus(@Param("usuarioId") Long usuarioId, @Param("status") StatusVenda status);
}