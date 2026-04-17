package br.com.ecommerce.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.ecommerce.model.pedido.Pedido;
import br.com.ecommerce.model.usuario.Usuario;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    @Query("""
            SELECT p
            FROM Pedido p
            WHERE p.id = :id
            """)
    @EntityGraph(attributePaths = { "produtos", "vendedor", "comprador" })
    Optional<Pedido> findByIdComProdutosEUsuarios(@Param(value = "id")Long id);

    @EntityGraph(attributePaths = { "produtos", "vendedor", "comprador" })
    Page<Pedido> findByComprador(Usuario usuario, Pageable pageable);

    @EntityGraph(attributePaths = { "produtos", "vendedor", "comprador" })
    Page<Pedido> findByVendedor(Usuario usuario, Pageable pageable);
}

