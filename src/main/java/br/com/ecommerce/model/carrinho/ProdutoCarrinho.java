package br.com.ecommerce.model.carrinho;

import java.math.BigDecimal;

import br.com.ecommerce.model.produto.Produto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "carrinho_produtos")
@Getter
@Setter
@NoArgsConstructor
public class ProdutoCarrinho {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer quantidade;
    private BigDecimal valorUnitario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrinho_id", nullable = false)
    private Carrinho carrinho;

    public ProdutoCarrinho(Produto produto, Integer quantidade, Carrinho carrinho) {
        this.quantidade = quantidade;
        this.valorUnitario = produto.getValor();
        this.produto = produto;
        this.carrinho = carrinho;
    }
}
