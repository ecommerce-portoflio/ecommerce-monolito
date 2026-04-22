package br.com.ecommerce.model.produto;

import java.math.BigDecimal;

public record DadosProduto(
    Long id,
    String nome,
    String descricao,
    Integer quantidadeEstoque,
    BigDecimal valor,
    Long idVendedor,
    Double mediaAvaliacoes,
    Integer quantidadeAvaliacoes
) {
    public DadosProduto(Produto produto) {
        this(produto.getId(), produto.getNome(), produto.getDescricao(), produto.getQuantidadeEstoque(), produto.getValor(),
                produto.getVendedor().getId(), produto.getMediaAvaliacoes(), produto.getQuantidadeAvaliacoes());
    }
}
