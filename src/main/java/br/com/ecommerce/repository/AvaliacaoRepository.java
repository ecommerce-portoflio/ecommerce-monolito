package br.com.ecommerce.repository;

import br.com.ecommerce.model.avaliacao.DadosAvaliacao;
import br.com.ecommerce.model.produto.Produto;
import br.com.ecommerce.model.usuario.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.ecommerce.model.avaliacao.Avaliacao;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    Optional<Avaliacao> findByAvaliadorAndProduto(Usuario usuario, Produto produto);

    @Query("""
            SELECT NEW br.com.ecommerce.model.avaliacao.DadosAvaliacao(
                a.produto.id,
                a.nota
            ) 
            FROM Avaliacao a
            WHERE a.avaliador = :usuario
            """)
    Page<DadosAvaliacao> findAllByAvaliador(@Param("usuario") Usuario usuario, Pageable pageable);
}

