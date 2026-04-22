package br.com.ecommerce.service;

import br.com.ecommerce.infra.exception.RegraDeNegocioException;
import br.com.ecommerce.model.avaliacao.Avaliacao;
import br.com.ecommerce.model.avaliacao.DadosCadastroAvaliacao;
import br.com.ecommerce.model.produto.Produto;
import br.com.ecommerce.model.usuario.Usuario;
import br.com.ecommerce.repository.AvaliacaoRepository;
import br.com.ecommerce.repository.ProdutoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class AvaliacaoService {
    private final AvaliacaoRepository avaliacaoRepository;
    private final ProdutoRepository produtoRepository;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository, ProdutoRepository produtoRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    public void avaliar(Usuario usuario, DadosCadastroAvaliacao dto) {
        Produto produto = produtoRepository.findByIdAndAtivo(dto.idProduto(), true)
                .orElseThrow(() -> new RegraDeNegocioException("Produto não encontrado!"));
        var avaliacaoBD = avaliacaoRepository.findByAvaliadorAndProduto(usuario, produto);
        if (avaliacaoBD.isPresent()) {
            produto.alterarAvaliacao(avaliacaoBD.get(), dto.nota());
            avaliacaoBD.get().setNota(dto.nota());
        }
        else {
            Avaliacao avaliacao = new Avaliacao(usuario, dto.nota(), produto);
            avaliacaoRepository.save(avaliacao);
            produto.avaliar(dto.nota());
        }
    }

    @Transactional
    public void removerAvaliacao(Usuario usuario, Long idProduto) {
        Produto produto = produtoRepository.findByIdAndAtivo(idProduto, true)
                .orElseThrow(() -> new RegraDeNegocioException("Produto não encontrado!"));
        var avaliacao = avaliacaoRepository.findByAvaliadorAndProduto(usuario, produto)
                .orElseThrow(() -> new RegraDeNegocioException("Você não avaliou esse produto!"));

        produto.removerAvaliacao(avaliacao);
        avaliacaoRepository.delete(avaliacao);
    }
}
