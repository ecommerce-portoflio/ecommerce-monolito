package br.com.ecommerce.model.pedido;

public record DadosProdutoPedido(
    Long produtoId,
    Long pedidoId
) {
    public DadosProdutoPedido(ProdutoPedido produtoPedido) {
        this(produtoPedido.getProduto().getId(), produtoPedido.getPedido().getId());
    }
}
