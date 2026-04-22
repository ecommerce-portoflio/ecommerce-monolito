package br.com.ecommerce.model.pedido;

public record DadosProdutoPedido(
    Long produtoId,
    Integer quantidade,
    Long pedidoId
) {
    public DadosProdutoPedido(ProdutoPedido produtoPedido) {
        this(produtoPedido.getProduto().getId(), produtoPedido.getQuantidade(), produtoPedido.getPedido().getId());
    }
}
