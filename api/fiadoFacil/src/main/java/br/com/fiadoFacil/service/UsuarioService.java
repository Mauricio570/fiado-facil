package br.com.fiadoFacil.service;

import br.com.fiadoFacil.domain.Usuario;
import br.com.fiadoFacil.dto.request.UsuarioAtualizacaoRequest;
import br.com.fiadoFacil.dto.request.UsuarioAtualizacaoRequest;
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
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          UsuarioMapper usuarioMapper,
                          PasswordEncoder passwordEncoder,
                          UsuarioAutenticadoService usuarioAutenticadoService) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
        this.passwordEncoder = passwordEncoder;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
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
    public UsuarioResponse buscarLogado() {
        return usuarioMapper.toResponse(usuarioAutenticadoService.get());
    }

    @Transactional
    public UsuarioResponse atualizarLogado(UsuarioAtualizacaoRequest request) {
        Usuario usuario = usuarioAutenticadoService.get();
        String emailNormalizado = request.getEmail().toLowerCase(Locale.ROOT);

        if (usuarioRepository.existsByEmailAndIdNot(emailNormalizado, usuario.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um cadastro com este e-mail.");
        }

        if (usuarioRepository.existsByCnpjAndIdNot(request.getCnpj(), usuario.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um cadastro com este CNPJ.");
        }

        if (querTrocarSenha(request)) {
            aplicarNovaSenha(usuario, request);
        }

        usuario.setNomeEmpresa(request.getNomeEmpresa());
        usuario.setCnpj(request.getCnpj());
        usuario.setEmail(emailNormalizado);

        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    private boolean querTrocarSenha(UsuarioAtualizacaoRequest request) {
        return request.getNovaSenha() != null && !request.getNovaSenha().isBlank();
    }

    // A senha atual é conferida antes de qualquer troca: sem isso, quem
    // encontrasse uma sessão aberta poderia assumir a conta trocando a senha.
    private void aplicarNovaSenha(Usuario usuario, UsuarioAtualizacaoRequest request) {
        if (request.getSenhaAtual() == null || request.getSenhaAtual().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe a senha atual para poder cadastrar uma nova senha.");
        }

        if (!passwordEncoder.matches(request.getSenhaAtual(), usuario.getSenha())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A senha atual está incorreta.");
        }

        if (!request.getNovaSenha().equals(request.getRepetirNovaSenha())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "As senhas informadas não coincidem.");
        }

        usuario.setSenha(passwordEncoder.encode(request.getNovaSenha()));
    }
}