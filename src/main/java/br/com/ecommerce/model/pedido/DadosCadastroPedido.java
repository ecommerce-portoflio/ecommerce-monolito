package br.com.ecommerce.model.pedido;

import jakarta.validation.constraints.Positive;

public record DadosCadastroPedido(
        Long produtoId,
        @Positive(message = "A quantidade do produto deve ser positiva!") Integer quantidade) {

}
