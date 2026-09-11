package br.com.fiadoFacil.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiadoFacil.domain.Preferencia;
import br.com.fiadoFacil.domain.Usuario;
import br.com.fiadoFacil.domain.enums.TamanhoTexto;
import br.com.fiadoFacil.dto.request.PreferenciaRequest;
import br.com.fiadoFacil.dto.response.PreferenciaResponse;
import br.com.fiadoFacil.repository.PreferenciaRepository;

@Service
public class PreferenciaService {

    private final PreferenciaRepository preferenciaRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public PreferenciaService(PreferenciaRepository preferenciaRepository,
                              UsuarioAutenticadoService usuarioAutenticadoService) {
        this.preferenciaRepository = preferenciaRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @Transactional
    public PreferenciaResponse buscar() {
        return toResponse(buscarOuCriar(usuarioAutenticadoService.get()));
    }

    @Transactional
    public PreferenciaResponse salvar(PreferenciaRequest request) {
        Preferencia preferencia = buscarOuCriar(usuarioAutenticadoService.get());
        preferencia.setTamanhoTexto(request.getTamanhoTexto());

        return toResponse(preferenciaRepository.save(preferencia));
    }

    /**
     * Contas criadas antes desta funcionalidade não têm linha em `preferencia`.
     * Em vez de falhar, a primeira leitura cria a linha com o padrão.
     */
    private Preferencia buscarOuCriar(Usuario usuario) {
        return preferenciaRepository.findByUsuarioId(usuario.getId())
                .orElseGet(() -> preferenciaRepository.save(Preferencia.builder()
                        .usuario(usuario)
                        .tamanhoTexto(TamanhoTexto.PADRAO)
                        .build()));
    }

    private PreferenciaResponse toResponse(Preferencia preferencia) {
        return PreferenciaResponse.builder()
                .tamanhoTexto(preferencia.getTamanhoTexto())
                .build();
    }
}
