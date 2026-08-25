package br.com.fiadoFacil.controller;

import br.com.fiadoFacil.dto.request.UsuarioCadastroRequest;
import br.com.fiadoFacil.dto.response.UsuarioResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.com.fiadoFacil.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping()
    public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody UsuarioCadastroRequest request) {
        UsuarioResponse response = usuarioService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }
}
