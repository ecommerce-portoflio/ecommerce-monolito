package br.com.ecommerce.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.ecommerce.model.usuario.Usuario;
import org.springframework.data.jpa.repository.Query;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseOrDocumentoIgnoreCase(String email, String documento);

    @Query("""
            SELECT COUNT(u) > 0
            FROM Usuario u
            WHERE LOWER(u.email) = LOWER(:email)
                OR LOWER(u.documento) = LOWER(:documento)
                OR LOWER(u.telefone) = LOWER(:telefone)
            """)
    boolean existePorDadosUnicos(String email, String documento, String telefone);
}

