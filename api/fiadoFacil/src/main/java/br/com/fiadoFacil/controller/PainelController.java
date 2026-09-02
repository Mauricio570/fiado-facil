package br.com.fiadoFacil.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiadoFacil.dto.response.PainelResponse;
import br.com.fiadoFacil.service.PainelService;

@RestController
@RequestMapping("/api/painel")
public class PainelController {

    private final PainelService painelService;

    public PainelController(PainelService painelService) {
        this.painelService = painelService;
    }

    @GetMapping
    public ResponseEntity<PainelResponse> buscarResumo() {
        return ResponseEntity.ok(painelService.buscarResumo());
    }
}