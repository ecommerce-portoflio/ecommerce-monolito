package br.com.ecommerce.repository;

import br.com.ecommerce.model.usuario.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.ecommerce.model.pedido.ProdutoPedido;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProdutoPedidoRepository extends JpaRepository<ProdutoPedido, Long> {
    @Query("""
            SELECT p
            FROM ProdutoPedido p
            WHERE p.produto.vendedor = :usuario
            """)
    Page<ProdutoPedido> buscarProdutosVendidos(@Param("usuario") Usuario usuario, Pageable pageable);
}

