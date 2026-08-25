package br.com.fiadoFacil.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiadoFacil.domain.Pagamento;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
    Optional<Pagamento> findByVendaId(Long vendaId);
}