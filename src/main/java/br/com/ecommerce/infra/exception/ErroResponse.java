package br.com.ecommerce.infra.exception;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ErroResponse {

    private LocalDateTime timestamp;
    private Integer status;
    private String error;
    private String message;
    private String path;

    public ErroResponse(Integer status, String error, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }
}