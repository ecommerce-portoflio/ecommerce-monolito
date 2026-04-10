package br.com.ecommerce.infra.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class TratadorDeErrosGlobais {

    // =========================
    // REGRA DE NEGÓCIO
    // =========================
    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroResponse> handleRegraDeNegocio(
            RegraDeNegocioException ex,
            HttpServletRequest request) {

        ErroResponse erro = new ErroResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Erro de regra de negócio",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    // =========================
    // VALIDAÇÃO (@Valid)
    // =========================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidacao(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> erros = new HashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            erros.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erros);
    }

    // =========================
    // JSON MAL FORMADO
    // =========================
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> handleJsonInvalido(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        ErroResponse erro = new ErroResponse(
                HttpStatus.BAD_REQUEST.value(),
                "JSON inválido",
                "Corpo da requisição mal formatado",
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(erro);
    }

    // =========================
    // TIPO DE PARÂMETRO INVÁLIDO
    // =========================
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> handleTipoInvalido(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        String mensagem = String.format(
                "Parâmetro '%s' com valor '%s' é inválido",
                ex.getName(),
                ex.getValue()
        );

        ErroResponse erro = new ErroResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Parâmetro inválido",
                mensagem,
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(erro);
    }

    // =========================
    // RECURSO NÃO ENCONTRADO
    // =========================
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErroResponse> handleRuntime(
            RuntimeException ex,
            HttpServletRequest request) {

        // Você pode customizar: ex: detectar "not found"
        ErroResponse erro = new ErroResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro interno",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
    }

    // =========================
    // ERRO GENÉRICO (fallback)
    // =========================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handleGeral(
            Exception ex,
            HttpServletRequest request) {

        ErroResponse erro = new ErroResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro inesperado",
                "Ocorreu um erro interno na aplicação",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
    }
}