package br.com.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.ecommerce.model.avaliacao.Avaliacao;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
}

