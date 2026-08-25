package br.com.fiadoFacil.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import br.com.fiadoFacil.dto.request.AuthLoginRequest;
import br.com.fiadoFacil.dto.response.AuthResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import br.com.fiadoFacil.domain.Usuario;
import br.com.fiadoFacil.repository.UsuarioRepository;

@Service
public class AuthService {

    private static final long EXPIRACAO_MINUTOS = 120;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtEncoder jwtEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
    }

    public AuthResponse login(AuthLoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail().toLowerCase(Locale.ROOT))
                .orElseThrow(this::credenciaisInvalidas);

        if (!passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
            throw credenciaisInvalidas();
        }

        Instant agora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("fiado-facil")
                .issuedAt(agora)
                .expiresAt(agora.plus(EXPIRACAO_MINUTOS, ChronoUnit.MINUTES))
                .subject(usuario.getId().toString())
                .claim("email", usuario.getEmail())
                .claim("nomeEmpresa", usuario.getNomeEmpresa())
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        return new AuthResponse(token, "Bearer", EXPIRACAO_MINUTOS * 60);
    }

    private ResponseStatusException credenciaisInvalidas() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos.");
    }
}