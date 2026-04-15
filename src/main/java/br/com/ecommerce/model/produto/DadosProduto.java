package br.com.ecommerce.model.produto;

import java.math.BigDecimal;

public record DadosProduto(
    Long id,
    String nome,
    String descricao,
    Integer quantidadeEstoque,
    BigDecimal valor,
    String nomeVendedor
) {
    public DadosProduto(Produto produto) {
        this(produto.getId(), produto.getNome(), produto.getDescricao(), produto.getQuantidadeEstoque(), produto.getValor(), produto.getVendedor().getNome());
    }
}
