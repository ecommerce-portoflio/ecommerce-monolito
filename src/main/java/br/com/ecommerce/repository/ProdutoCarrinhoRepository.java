package br.com.ecommerce.repository;

import br.com.ecommerce.model.carrinho.Carrinho;
import br.com.ecommerce.model.produto.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.ecommerce.model.carrinho.ProdutoCarrinho;

import java.util.List;
import java.util.Optional;

public interface ProdutoCarrinhoRepository extends JpaRepository<ProdutoCarrinho, Long> {
    Optional<ProdutoCarrinho> findByProdutoAndCarrinho(Produto produto, Carrinho carrinho);

    List<ProdutoCarrinho> findByCarrinhoAndProdutoIn(Carrinho carrinho, List<Produto> listaProdutos);
}

