package br.com.ecommerce.model.avaliacao;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record DadosCadastroAvaliacao(
        @NotNull(message = "Preencha o ID do produto!")
        Long idProduto,
        @PositiveOrZero(message = "A nota deve ser positiva ou maior que zero!")
        @Max(value = 5, message = "A nota máxima é 5!")
        Double nota
) {
}
