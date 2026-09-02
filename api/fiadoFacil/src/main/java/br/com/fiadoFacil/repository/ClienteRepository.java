package br.com.fiadoFacil.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.fiadoFacil.domain.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByIdAndUsuarioId(Long id, Long usuarioId);

    long countByUsuarioId(Long usuarioId);

    @Query("SELECT c FROM Cliente c WHERE c.usuario.id = :usuarioId " +
            "AND (:busca IS NULL OR :busca = '' " +
            "OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :busca, '%')) " +
            "OR c.telefone LIKE CONCAT('%', :busca, '%')) " +
            "ORDER BY c.nome ASC")
    List<Cliente> buscarPorUsuario(@Param("usuarioId") Long usuarioId, @Param("busca") String busca);

    boolean existsByUsuarioIdAndCpf(Long usuarioId, String cpf);

    boolean existsByUsuarioIdAndTelefone(Long usuarioId, String telefone);

    boolean existsByUsuarioIdAndCpfAndIdNot(Long usuarioId, String cpf, Long id);

    boolean existsByUsuarioIdAndTelefoneAndIdNot(Long usuarioId, String telefone, Long id);
}