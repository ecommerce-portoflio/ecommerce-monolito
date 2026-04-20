package br.com.ecommerce.model.carrinho;

import br.com.ecommerce.model.produto.DadosProduto;

import java.math.BigDecimal;
import java.util.List;

public record DadosCarrinho(
        BigDecimal total,
        List<DadosProdutoCarrinho> produtos
) {
    public DadosCarrinho(Carrinho carrinho) {
        this(carrinho.getTotal(),
                carrinho.getProdutos()
                .stream()
                .map(DadosProdutoCarrinho::new)
                .toList());
    }
}
