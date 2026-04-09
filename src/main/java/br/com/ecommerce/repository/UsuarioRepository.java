package br.com.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.ecommerce.model.usuario.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}

