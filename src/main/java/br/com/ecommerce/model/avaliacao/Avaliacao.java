package br.com.ecommerce.model.avaliacao;

import br.com.ecommerce.model.produto.Produto;
import br.com.ecommerce.model.usuario.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "avaliacoes")
@Getter
@Setter
@NoArgsConstructor
public class Avaliacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double nota;

    @ManyToOne
    @JoinColumn(name = "avaliador_id", nullable = false)
    private Usuario avaliador;

    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    public Avaliacao(Usuario usuario, Double nota, Produto produto) {
        avaliador = usuario;
        this.nota = nota;
        this.produto = produto;
    }
}
