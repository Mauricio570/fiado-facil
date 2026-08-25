package br.com.fiadoFacil.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiadoFacil.dto.request.VendaRequest;
import br.com.fiadoFacil.dto.response.VendaResponse;
import br.com.fiadoFacil.dto.response.VendaResumoResponse;
import br.com.fiadoFacil.service.VendaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/vendas")
public class VendaController {

    private final VendaService vendaService;

    public VendaController(VendaService vendaService) {
        this.vendaService = vendaService;
    }

    @PostMapping
    public ResponseEntity<VendaResponse> cadastrar(@Valid @RequestBody VendaRequest request) {
        VendaResponse response = vendaService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<VendaResumoResponse>> listarPorCliente(@RequestParam Long clienteId) {
        return ResponseEntity.ok(vendaService.listarPorCliente(clienteId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendaResponse> buscarDetalhe(@PathVariable Long id) {
        return ResponseEntity.ok(vendaService.buscarDetalhe(id));
    }
}