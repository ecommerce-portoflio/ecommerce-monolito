package br.com.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.ecommerce.model.carrinho.ProdutoCarrinho;

public interface ProdutoCarrinhoRepository extends JpaRepository<ProdutoCarrinho, Long> {
}

