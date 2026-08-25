package br.com.fiadoFacil.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import br.com.fiadoFacil.domain.Usuario;
import br.com.fiadoFacil.repository.UsuarioRepository;

@Service
public class UsuarioAutenticadoService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioAutenticadoService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public String getEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getCredentials();
        return jwt.getClaim("email");
    }

    public Usuario get() {
        return usuarioRepository.findByEmail(getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Usuário autenticado não encontrado no banco (token válido, mas conta pode ter sido removida)."));
    }
}