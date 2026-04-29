package br.com.ecommerce.controller;

import br.com.ecommerce.model.avaliacao.DadosCadastroAvaliacao;
import br.com.ecommerce.model.usuario.Usuario;
import br.com.ecommerce.service.AvaliacaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/avaliacao")
public class AvaliacaoController {
    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @PostMapping
    public ResponseEntity<String> avaliarProduto(@AuthenticationPrincipal Usuario usuario,
                                                 @RequestBody @Valid DadosCadastroAvaliacao dto) {
        avaliacaoService.avaliar(usuario, dto);
        return ResponseEntity.ok("Avaliação registrada!");
    }

    @DeleteMapping("/{idProduto}")
    public ResponseEntity<String> removerAvaliacao(@AuthenticationPrincipal Usuario usuario,
                                                   @PathVariable Long idProduto) {
        avaliacaoService.removerAvaliacao(usuario, idProduto);
        return ResponseEntity.ok("Avaliação excluída!");
    }

//    TODO: Buscar meus produtos avaliados
}
