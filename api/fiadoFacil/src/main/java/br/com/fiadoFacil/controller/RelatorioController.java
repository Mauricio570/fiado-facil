package br.com.fiadoFacil.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiadoFacil.dto.response.RelatorioClientesResponse;
import br.com.fiadoFacil.dto.response.RelatorioVendaResponse;
import br.com.fiadoFacil.dto.response.RelatorioVisaoGeralResponse;
import br.com.fiadoFacil.service.RelatorioService;

/**
 * Um endpoint por aba da tela de relatórios. As datas do período são
 * opcionais (aaaa-mm-dd); sem elas o relatório usa os últimos seis meses.
 */
@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/visao-geral")
    public ResponseEntity<RelatorioVisaoGeralResponse> buscarVisaoGeral(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(relatorioService.buscarVisaoGeral(inicio, fim));
    }

    @GetMapping("/clientes")
    public ResponseEntity<RelatorioClientesResponse> buscarClientes() {
        return ResponseEntity.ok(relatorioService.buscarClientes());
    }

    @GetMapping("/vendas")
    public ResponseEntity<List<RelatorioVendaResponse>> listarVendas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(relatorioService.listarVendas(inicio, fim));
    }
}
