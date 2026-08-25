package br.com.fiadoFacil.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiadoFacil.domain.Venda;

public interface VendaRepository extends JpaRepository<Venda, Long> {

    Optional<Venda> findByIdAndCliente_Usuario_Id(Long id, Long usuarioId);

    List<Venda> findAllByCliente_IdAndCliente_Usuario_IdOrderByDataCriacaoDesc(Long clienteId, Long usuarioId);
}