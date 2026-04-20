package br.com.ecommerce.repository;

import br.com.ecommerce.model.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.ecommerce.model.carrinho.Carrinho;

public interface CarrinhoRepository extends JpaRepository<Carrinho, Long> {
    Carrinho findByUsuario(Usuario usuario);
}

