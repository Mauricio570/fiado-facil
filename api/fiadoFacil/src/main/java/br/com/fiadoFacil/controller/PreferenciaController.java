package br.com.fiadoFacil.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiadoFacil.dto.request.PreferenciaRequest;
import br.com.fiadoFacil.dto.response.PreferenciaResponse;
import br.com.fiadoFacil.service.PreferenciaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/preferencias")
public class PreferenciaController {

    private final PreferenciaService preferenciaService;

    public PreferenciaController(PreferenciaService preferenciaService) {
        this.preferenciaService = preferenciaService;
    }

    @GetMapping
    public ResponseEntity<PreferenciaResponse> buscar() {
        return ResponseEntity.ok(preferenciaService.buscar());
    }

    @PutMapping
    public ResponseEntity<PreferenciaResponse> salvar(@Valid @RequestBody PreferenciaRequest request) {
        return ResponseEntity.ok(preferenciaService.salvar(request));
    }
}
