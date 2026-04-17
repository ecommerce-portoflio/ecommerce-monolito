package br.com.ecommerce.model.pedido;

public enum StatusPedido {
    EM_ANDAMENTO, // Seria implementado com um frontend, quando o usuário juntar os produtos desejados, mas não fazer o pedido
    AGUARDANDO_PAGAMENTO,
    PAGO,
    ENTREGUE
}
