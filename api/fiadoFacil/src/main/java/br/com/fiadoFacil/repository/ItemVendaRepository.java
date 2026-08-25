package br.com.fiadoFacil.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiadoFacil.domain.ItemVenda;

public interface ItemVendaRepository extends JpaRepository<ItemVenda, Long> {
    List<ItemVenda> findAllByVendaId(Long vendaId);
}