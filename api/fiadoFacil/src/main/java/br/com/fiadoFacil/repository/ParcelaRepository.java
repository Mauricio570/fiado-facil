package br.com.fiadoFacil.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiadoFacil.domain.Parcela;

public interface ParcelaRepository extends JpaRepository<Parcela, Long> {
    List<Parcela> findAllByPagamentoIdOrderByNumeroAsc(Long pagamentoId);
}