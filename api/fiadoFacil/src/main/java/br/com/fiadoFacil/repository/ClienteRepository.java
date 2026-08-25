package br.com.fiadoFacil.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.fiadoFacil.domain.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Toda busca/edição/exclusão de cliente passa por aqui — garante
    // que o cliente pertence ao usuário logado, não só que o id existe.
    Optional<Cliente> findByIdAndUsuarioId(Long id, Long usuarioId);

    @Query("SELECT c FROM Cliente c WHERE c.usuario.id = :usuarioId " +
            "AND (:busca IS NULL OR :busca = '' " +
            "OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :busca, '%')) " +
            "OR c.telefone LIKE CONCAT('%', :busca, '%')) " +
            "ORDER BY c.nome ASC")
    List<Cliente> buscarPorUsuario(@Param("usuarioId") Long usuarioId, @Param("busca") String busca);

    // Verificação de duplicidade no cadastro (cliente novo, sem id ainda)
    boolean existsByUsuarioIdAndCpf(Long usuarioId, String cpf);
    boolean existsByUsuarioIdAndTelefone(Long usuarioId, String telefone);

    // Mesma verificação na edição, mas ignorando o próprio cliente
    // (senão ele sempre "bateria" duplicado com ele mesmo)
    boolean existsByUsuarioIdAndCpfAndIdNot(Long usuarioId, String cpf, Long id);
    boolean existsByUsuarioIdAndTelefoneAndIdNot(Long usuarioId, String telefone, Long id);
}