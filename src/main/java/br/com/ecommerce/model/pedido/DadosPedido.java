package br.com.ecommerce.model.pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record DadosPedido(
    Long id,
    BigDecimal valorTotal,
    LocalDateTime dataCompra,
    StatusPedido statusPedido,
    Long compradorId,
    List<DadosProdutoPedido> produtos
) {
    public DadosPedido(Pedido pedido) {
        this(pedido.getId(), pedido.getValorTotal(), pedido.getDataCompra(), pedido.getStatusPedido(),
                pedido.getComprador().getId(),
                pedido.getProdutos()
                        .stream()
                        .map(DadosProdutoPedido::new)
                        .toList());
    }
}
