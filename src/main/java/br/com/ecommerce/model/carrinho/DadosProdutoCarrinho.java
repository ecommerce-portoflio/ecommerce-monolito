package br.com.ecommerce.model.carrinho;

import java.math.BigDecimal;

public record DadosProdutoCarrinho(
        Long idProduto,
        Integer quantidade,
        BigDecimal valorUnitario
) {
    public DadosProdutoCarrinho(ProdutoCarrinho produtoCarrinho) {
        this(produtoCarrinho.getProduto().getId(), produtoCarrinho.getQuantidade(), produtoCarrinho.getValorUnitario());
    }
}
