package br.com.fiadoFacil.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiadoFacil.dto.response.ParcelaResponse;
import br.com.fiadoFacil.service.ParcelaService;

@RestController
@RequestMapping("/api/parcelas")
public class ParcelaController {

    private final ParcelaService parcelaService;

    public ParcelaController(ParcelaService parcelaService) {
        this.parcelaService = parcelaService;
    }

    @PatchMapping("/{id}/pagar")
    public ResponseEntity<ParcelaResponse> marcarComoPaga(@PathVariable Long id) {
        return ResponseEntity.ok(parcelaService.marcarComoPaga(id));
    }
}