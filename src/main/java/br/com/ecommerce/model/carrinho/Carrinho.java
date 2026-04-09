package br.com.ecommerce.model.carrinho;

import java.math.BigDecimal;
import java.util.List;

import br.com.ecommerce.model.usuario.Usuario;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "carrinhos")
@Getter
@Setter
@NoArgsConstructor
public class Carrinho {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private BigDecimal total;
    
    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;
    
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "carrinho", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProdutoCarrinho> produtos;
}
