package br.com.ecommerce.model.produto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

import org.hibernate.validator.constraints.Length;

public record DadosCadastroProduto(
        @NotBlank(message = "Preencha o nome!")
        @Length(min = 3, message = "O nome deve ter no mínimo 3 caracteres!")
        String nome,
        @NotNull(message = "Preencha a descrição!")
        @Length(min = 10, message = "A descrição deve ter no mínimo 10 caracteres!")
        String descricao,
        @PositiveOrZero(message = "A quantidade em estoque deve ser maior que 0!")
        Integer quantidadeEstoque,
        @PositiveOrZero(message = "O valor deve ser maior que 0!")
        BigDecimal valor
) {
}
