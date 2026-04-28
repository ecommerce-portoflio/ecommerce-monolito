package br.com.ecommerce.model.pedido;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DadosCadastroPedido(
        @NotNull(message = "Informe o ID do produto!")
        Long produtoId,
        @Positive(message = "A quantidade do produto deve ser positiva!")
        @NotNull(message = "Informe a quantidade!")
        Integer quantidade) {

}
