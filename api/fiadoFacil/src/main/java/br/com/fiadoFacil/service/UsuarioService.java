package br.com.fiadoFacil.service;

import br.com.fiadoFacil.domain.Usuario;
import br.com.fiadoFacil.dto.request.UsuarioCadastroRequest;
import br.com.fiadoFacil.dto.response.UsuarioResponse;
import jakarta.transaction.Transactional;
import br.com.fiadoFacil.mapper.UsuarioMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import br.com.fiadoFacil.repository.UsuarioRepository;

import java.util.Locale;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          UsuarioMapper usuarioMapper,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponse cadastrar(UsuarioCadastroRequest request) {
        if (!request.getSenha().equals(request.getRepetirSenha())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "As senhas informadas não coincidem.");
        }

        String emailNormalizado = request.getEmail().toLowerCase(Locale.ROOT);

        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um cadastro com este e-mail.");
        }

        if (usuarioRepository.existsByCnpj(request.getCnpj())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um cadastro com este CNPJ.");
        }

        Usuario usuario = Usuario.builder()
                .nomeEmpresa(request.getNomeEmpresa())
                .cnpj(request.getCnpj())
                .email(emailNormalizado)
                .senha(passwordEncoder.encode(request.getSenha()))
                .build();

        Usuario salvo = usuarioRepository.save(usuario);
        return usuarioMapper.toResponse(salvo);
    }

    @Transactional
    public UsuarioResponse buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        return usuarioMapper.toResponse(usuario);
    }
}