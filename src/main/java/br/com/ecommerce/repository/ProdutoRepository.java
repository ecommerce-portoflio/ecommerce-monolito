package br.com.ecommerce.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.ecommerce.model.produto.Produto;
import br.com.ecommerce.model.usuario.Usuario;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    Page<Produto> findAllByAtivo(Pageable pageable, boolean ativo);
    Page<Produto> findAllByVendedorAndAtivo(Usuario vendedor, Pageable pageable, boolean ativo);
    Optional<Produto> findByIdAndAtivo(Long id, boolean ativo);
}

