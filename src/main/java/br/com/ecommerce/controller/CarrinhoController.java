package br.com.ecommerce.controller;

import br.com.ecommerce.model.carrinho.DadosCarrinho;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.com.ecommerce.model.carrinho.DadosCadastroProdutoCarrinho;
import br.com.ecommerce.model.usuario.Usuario;
import br.com.ecommerce.service.CarrinhoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;


@RestController
@RequestMapping("/carrinho")
public class CarrinhoController {
    private final CarrinhoService carrinhoService;

    public CarrinhoController(CarrinhoService carrinhoService) {
        this.carrinhoService = carrinhoService;
    }
    
    @PostMapping
    public ResponseEntity<String> adicionarProdutoCarrinho(@RequestBody DadosCadastroProdutoCarrinho dto,
                                                   @AuthenticationPrincipal Usuario usuario) {
        carrinhoService.adicionarProduto(dto, usuario);
        return ResponseEntity.ok("Produto adicionado ao carrinho!");
    }

    @GetMapping
    public ResponseEntity<DadosCarrinho> buscarCarrinho(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(carrinhoService.buscarCarrinho(usuario));
    }
    
    @DeleteMapping("/{idProduto}")
    public ResponseEntity<String> removerProdutoCarrinho(@PathVariable Long idProduto, @AuthenticationPrincipal Usuario usuario) {
        carrinhoService.removerProduto(idProduto, usuario);
        return ResponseEntity.ok("Produto removido do carrinho!");
    }

//    TODO: Remover lista de produtos do carrinho

    @DeleteMapping("/todos")
    public ResponseEntity<String> esvaziarCarrinho(@AuthenticationPrincipal Usuario usuario) {
        carrinhoService.esvaziarCarrinho(usuario);
        return ResponseEntity.ok("Carrinho esavaziado!");
    }
}
