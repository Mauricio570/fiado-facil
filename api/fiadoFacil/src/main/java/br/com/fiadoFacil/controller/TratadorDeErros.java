package br.com.fiadoFacil.controller;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import br.com.fiadoFacil.dto.response.ErroResponse;

/**
 * Centraliza a resposta de erro de toda a API em um formato único, com uma
 * mensagem já pronta para ser exibida ao usuário. Sem isso o Spring devolve
 * apenas o status e o path, e as mensagens escritas nos services nunca chegam
 * ao front-end.
 */
@RestControllerAdvice
public class TratadorDeErros {

    private static final Logger log = LoggerFactory.getLogger(TratadorDeErros.class);

    /** Erros de regra de negócio lançados pelos services (duplicidade, conflito, não encontrado). */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErroResponse> tratarErroDeRegra(ResponseStatusException excecao) {
        HttpStatus status = HttpStatus.valueOf(excecao.getStatusCode().value());
        String mensagem = excecao.getReason() != null ? excecao.getReason() : mensagemPadrao(status);

        return ResponseEntity.status(status).body(montar(status, mensagem, null));
    }

    /** Falha de Bean Validation no corpo da requisição. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarErroDeValidacao(MethodArgumentNotValidException excecao) {
        Map<String, String> campos = new LinkedHashMap<>();

        excecao.getBindingResult().getFieldErrors()
                .forEach(erro -> campos.putIfAbsent(erro.getField(), erro.getDefaultMessage()));

        return ResponseEntity.badRequest()
                .body(montar(HttpStatus.BAD_REQUEST, "Verifique os campos informados.", campos));
    }

    /**
     * Rota inexistente. Sem este tratador a exceção cairia na rede de
     * segurança abaixo e uma URL errada viraria 500 em vez de 404.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponse> tratarRotaInexistente(NoResourceFoundException excecao) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(montar(HttpStatus.NOT_FOUND, "Endereço não encontrado na API."));
    }

    /** Método HTTP que a rota não aceita (por exemplo, POST onde só há GET). */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponse> tratarMetodoInvalido(
            HttpRequestMethodNotSupportedException excecao) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(montar(HttpStatus.METHOD_NOT_ALLOWED,
                        "Esta operação não é permitida neste endereço."));
    }

    /** JSON malformado ou tipo de campo incompatível. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> tratarCorpoInvalido(HttpMessageNotReadableException excecao) {
        log.warn("Corpo de requisição inválido: {}", excecao.getMessage());

        return ResponseEntity.badRequest()
                .body(montar(HttpStatus.BAD_REQUEST, "Não foi possível ler os dados enviados.", null));
    }

    /**
     * Rede de segurança para o que não foi previsto. A causa real vai para o
     * log; a resposta não expõe detalhes internos ao cliente.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> tratarErroInesperado(Exception excecao) {
        log.error("Erro inesperado ao processar a requisição", excecao);

        return ResponseEntity.internalServerError()
                .body(montar(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Ocorreu um erro inesperado. Tente novamente em instantes.", null));
    }

    private ErroResponse montar(HttpStatus status, String mensagem) {
        return montar(status, mensagem, null);
    }

    private ErroResponse montar(HttpStatus status, String mensagem, Map<String, String> campos) {
        return ErroResponse.builder()
                .momento(LocalDateTime.now())
                .status(status.value())
                .mensagem(mensagem)
                .campos(campos)
                .build();
    }

    private String mensagemPadrao(HttpStatus status) {
        return status == HttpStatus.NOT_FOUND
                ? "Registro não encontrado."
                : "Não foi possível concluir a operação.";
    }
}
