package br.com.fiadoFacil.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiadoFacil.domain.Preferencia;

public interface PreferenciaRepository extends JpaRepository<Preferencia, Long> {

    Optional<Preferencia> findByUsuarioId(Long usuarioId);
}
