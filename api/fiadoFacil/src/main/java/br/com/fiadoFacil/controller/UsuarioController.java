package br.com.fiadoFacil.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiadoFacil.dto.request.UsuarioAtualizacaoRequest;
import br.com.fiadoFacil.dto.request.UsuarioCadastroRequest;
import br.com.fiadoFacil.dto.response.UsuarioResponse;
import br.com.fiadoFacil.service.UsuarioService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody UsuarioCadastroRequest request) {
        UsuarioResponse response = usuarioService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Os dados da conta são sempre resolvidos pelo token, nunca por um id vindo
     * da URL — assim um usuário não consegue ler nem alterar a conta de outro.
     */
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> buscarLogado() {
        return ResponseEntity.ok(usuarioService.buscarLogado());
    }

    @PutMapping("/me")
    public ResponseEntity<UsuarioResponse> atualizarLogado(
            @Valid @RequestBody UsuarioAtualizacaoRequest request) {
        return ResponseEntity.ok(usuarioService.atualizarLogado(request));
    }
}
