package br.com.ecommerce.model.avaliacao;

import br.com.ecommerce.model.produto.Produto;
import br.com.ecommerce.model.usuario.Usuario;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
    private Usuario avaliador;

    @ManyToOne
    private Produto produto;

    public Avaliacao(Usuario usuario, Double nota, Produto produto) {
        avaliador = usuario;
        this.nota = nota;
        this.produto = produto;
    }
}
