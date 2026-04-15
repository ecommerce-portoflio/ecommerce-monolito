package br.com.ecommerce.model.produto;

import java.math.BigDecimal;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.NotNull;

public record DadosAtualizarProduto(
    @NotNull(message = "O ID é obrigatório!")
    Long id,
    @NotBlank(message = "Preencha o nome!")
    @Length(min = 3, message = "O nome deve ter no mínimo 3 caracteres!")
    String nome,
    @NotBlank(message = "Preencha a descrição!")
    @Length(min = 10, message = "A descrição deve ter no mínimo 10 caracteres!")
    String descricao,
    @PositiveOrZero(message = "A quantidade em estoque deve ser maior que 0!")
    Integer quantidadeEstoque,
    @PositiveOrZero(message = "O valor deve ser maior que 0!")
    BigDecimal valor
) {
    
}
