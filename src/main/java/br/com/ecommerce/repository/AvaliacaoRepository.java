package br.com.ecommerce.repository;

import br.com.ecommerce.model.produto.Produto;
import br.com.ecommerce.model.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.ecommerce.model.avaliacao.Avaliacao;

import java.util.Optional;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    Optional<Avaliacao> findByAvaliadorAndProduto(Usuario usuario, Produto produto);
}

