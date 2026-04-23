package br.com.ecommerce.service;

import br.com.ecommerce.infra.exception.RegraDeNegocioException;
import br.com.ecommerce.model.carrinho.DadosCarrinho;
import br.com.ecommerce.model.carrinho.ProdutoCarrinho;
import br.com.ecommerce.model.produto.Produto;
import br.com.ecommerce.repository.ProdutoCarrinhoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import br.com.ecommerce.model.carrinho.Carrinho;
import br.com.ecommerce.model.carrinho.DadosCadastroProdutoCarrinho;
import br.com.ecommerce.model.usuario.Usuario;
import br.com.ecommerce.repository.CarrinhoRepository;
import br.com.ecommerce.repository.ProdutoRepository;

import java.math.BigDecimal;
import java.util.Objects;

@Service
public class CarrinhoService {
    private final CarrinhoRepository carrinhoRepository;
    private final ProdutoRepository produtoRepository;
    private final ProdutoCarrinhoRepository produtoCarrinhoRepository;

    public CarrinhoService(CarrinhoRepository carrinhoRepository, ProdutoRepository produtoRepository, ProdutoCarrinhoRepository produtoCarrinhoRepository) {
        this.carrinhoRepository = carrinhoRepository;
        this.produtoRepository = produtoRepository;
        this.produtoCarrinhoRepository = produtoCarrinhoRepository;
    }

    @Transactional
    public void adicionarProduto(DadosCadastroProdutoCarrinho dto, Usuario usuario) {
        Carrinho carrinho = retornaCarrinho(usuario);
        var produto = produtoRepository.findByIdAndAtivo(dto.idProduto(), true)
                .orElseThrow(() -> new RegraDeNegocioException("Produto não encontrado!"));

        validacoesAdicionarProdutoCarrinho(produto, usuario, dto);

        carrinho.adicionarProduto(produto, dto.quantidade());
    }

    public DadosCarrinho buscarCarrinho(Usuario usuario) {
        Carrinho carrinho = retornaCarrinho(usuario);
        return new DadosCarrinho(carrinho);
    }

    @Transactional
    public void removerProduto(Long idProduto, Usuario usuario) {
        Carrinho carrinho = retornaCarrinho(usuario);
        Produto produto = produtoRepository.findByIdAndAtivo(idProduto, true)
                .orElseThrow(() -> new RegraDeNegocioException("Produto não encontrado!"));
        ProdutoCarrinho produtoCarrinho = produtoCarrinhoRepository.findByProdutoAndCarrinho(produto, carrinho)
                .orElseThrow(() -> new RegraDeNegocioException("Produto informado não se encontra no seu carrinho!"));
        carrinho.removerProduto(produtoCarrinho);
    }

    @Transactional
    public void esvaziarCarrinho(Usuario usuario) {
        Carrinho carrinho = retornaCarrinho(usuario);
        produtoCarrinhoRepository.deleteAllByCarrinho(carrinho);
        carrinho.setTotal(BigDecimal.ZERO);
    }

    private Carrinho retornaCarrinho(Usuario usuario) {
        return carrinhoRepository.findByUsuario(usuario);
    }

    private void validacoesAdicionarProdutoCarrinho(Produto produto, Usuario usuario, DadosCadastroProdutoCarrinho dto) {
        if (Objects.equals(produto.getVendedor().getId(), usuario.getId()))
            throw new RegraDeNegocioException("Você não pode adicionar seu próprio produto no carrinho!");

        if (produto.getQuantidadeEstoque() < dto.quantidade())
            throw new RegraDeNegocioException("A quantidade desejada não está disponível para o atual estoque do produto!");
    }
}
