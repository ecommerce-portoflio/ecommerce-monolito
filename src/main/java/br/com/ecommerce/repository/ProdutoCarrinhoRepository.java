package br.com.ecommerce.repository;

import br.com.ecommerce.model.carrinho.Carrinho;
import br.com.ecommerce.model.produto.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.ecommerce.model.carrinho.ProdutoCarrinho;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProdutoCarrinhoRepository extends JpaRepository<ProdutoCarrinho, Long> {
    void deleteAllByCarrinho(Carrinho carrinho);

    Optional<ProdutoCarrinho> findByProdutoAndCarrinho(Produto produto, Carrinho carrinho);
}

