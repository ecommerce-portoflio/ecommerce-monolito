package br.com.ecommerce.model.produto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import br.com.ecommerce.model.avaliacao.Avaliacao;
import br.com.ecommerce.model.usuario.Usuario;
import jakarta.persistence.*;
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
    
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "produto", orphanRemoval = true)
    private List<Avaliacao> avaliacoes = new ArrayList<>();
    
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

    public void avaliar(Avaliacao avaliacao) {
        avaliacoes.add(avaliacao);
        double novaMedia = ((mediaAvaliacoes * quantidadeAvaliacoes) + avaliacao.getNota()) / (quantidadeAvaliacoes + 1);
        quantidadeAvaliacoes++;
        mediaAvaliacoes = arrendonda2Casas(novaMedia);
    }

    public void alterarAvaliacao(Avaliacao avaliacao, Double nota) {
        double novaMedia = ((mediaAvaliacoes * quantidadeAvaliacoes) - avaliacao.getNota() + nota) / quantidadeAvaliacoes;
        mediaAvaliacoes = arrendonda2Casas(novaMedia);
    }

    public void removerAvaliacao(Avaliacao avaliacao) {
        avaliacoes.remove(avaliacao);
        avaliacao.setProduto(null);

        if (quantidadeAvaliacoes == 1)
            mediaAvaliacoes = 0.0;
        else {
            double novaMedia = ((mediaAvaliacoes * quantidadeAvaliacoes) - avaliacao.getNota()) / (quantidadeAvaliacoes - 1);
            mediaAvaliacoes = arrendonda2Casas(novaMedia);
        }
        quantidadeAvaliacoes--;
    }

    private double arrendonda2Casas(double novaMedia) {
        return BigDecimal
                .valueOf(novaMedia)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
