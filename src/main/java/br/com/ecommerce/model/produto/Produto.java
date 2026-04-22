package br.com.ecommerce.model.produto;

import java.math.BigDecimal;
import java.util.List;

import br.com.ecommerce.model.avaliacao.Avaliacao;
import br.com.ecommerce.model.usuario.Usuario;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
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
@Table(name = "produtos")
@Getter
@Setter
@NoArgsConstructor
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String descricao;
    private Integer quantidadeEstoque;
    private BigDecimal valor;
    private Double mediaAvaliacoes;
    private Integer quantidadeAvaliacoes;
    private boolean ativo;
    
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "produto")
    private List<Avaliacao> avaliacoes;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendedor_id", nullable = false)
    private Usuario vendedor;

    public Produto(DadosCadastroProduto dto, Usuario logado) {
        nome = dto.nome();
        descricao = dto.descricao();
        quantidadeEstoque = dto.quantidadeEstoque();
        valor = dto.valor();
        vendedor = logado;
        ativo = true;
        mediaAvaliacoes = 0.0;
        quantidadeAvaliacoes = 0;
    }

    public void atualizar(DadosAtualizarProduto dto) {
        if (dto.nome() != null && !dto.nome().isBlank())
            this.nome = dto.nome();
        if (dto.descricao() != null && !dto.descricao().isBlank())
            this.descricao = dto.descricao();
        if (dto.quantidadeEstoque() != null)
            this.quantidadeEstoque = dto.quantidadeEstoque();
        if (dto.valor() != null)
            this.valor = dto.valor();
    }

    public void avaliar(Double nota) {
        double novaMedia = ((mediaAvaliacoes * quantidadeAvaliacoes) + nota) / (quantidadeAvaliacoes + 1);
        quantidadeAvaliacoes++;
        mediaAvaliacoes = novaMedia;
    }

    public void alterarAvaliacao(Avaliacao avaliacao, Double nota) {
        mediaAvaliacoes = ((mediaAvaliacoes * quantidadeAvaliacoes) - avaliacao.getNota() + nota) / quantidadeAvaliacoes;
    }

    public void removerAvaliacao(Avaliacao avaliacao) {
        if (quantidadeAvaliacoes == 1)
            mediaAvaliacoes = 0.0;
        else
            mediaAvaliacoes = ((mediaAvaliacoes * quantidadeAvaliacoes) - avaliacao.getNota()) / quantidadeAvaliacoes - 1;
        quantidadeAvaliacoes--;
    }
}
