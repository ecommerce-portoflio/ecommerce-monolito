package br.com.ecommerce.model.pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.com.ecommerce.model.carrinho.Carrinho;
import br.com.ecommerce.model.produto.Produto;
import br.com.ecommerce.model.usuario.Usuario;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private BigDecimal valorTotal;
    private LocalDateTime dataCompra;
    @Enumerated(EnumType.STRING)
    private StatusPedido statusPedido;

    @ManyToOne
    @JoinColumn(name = "comprador_id", nullable = false)
    private Usuario comprador;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProdutoPedido> produtos = new ArrayList<>();

    // Pedido de um produto só
    public Pedido(Produto produto, Usuario comprador, Integer quantidade) {
        this.comprador = comprador;
        this.statusPedido = StatusPedido.AGUARDANDO_PAGAMENTO;
        this.valorTotal = produto.getValor().multiply(BigDecimal.valueOf(quantidade));
        this.produtos.add(new ProdutoPedido(produto, this, quantidade));
        // Data da compra só é preenchida após pagamento
    }

//    Pedido a partir de um carrinho
    public Pedido(Carrinho carrinho) {
        comprador = carrinho.getUsuario();
        statusPedido = StatusPedido.AGUARDANDO_PAGAMENTO;
        valorTotal = carrinho.getTotal();
        carrinho.getProdutos().forEach(produtoCarrinho -> produtos
                .add(new ProdutoPedido(produtoCarrinho.getProduto(), this, produtoCarrinho.getQuantidade())));
    }

//    Pedido a partir de múltiplos produtos
    public Pedido(Usuario usuario) {
        comprador = usuario;
        statusPedido = StatusPedido.AGUARDANDO_PAGAMENTO;
        valorTotal = BigDecimal.ZERO;
    }

    public void acrescentarProduto(Produto produto, Integer quantidade) {
        produtos.add(new ProdutoPedido(produto, this, quantidade));
        valorTotal = valorTotal.add(produto.getValor()
                .multiply(BigDecimal.valueOf(quantidade)));
    }
}
