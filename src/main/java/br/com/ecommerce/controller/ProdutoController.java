package br.com.ecommerce.controller;

import br.com.ecommerce.model.produto.DadosAtualizarProduto;
import br.com.ecommerce.model.produto.DadosCadastroProduto;
import br.com.ecommerce.model.produto.DadosProduto;
import br.com.ecommerce.model.usuario.Usuario;
import br.com.ecommerce.service.ProdutoService;
import jakarta.validation.Valid;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;


@RestController
@RequestMapping("/produto")
public class ProdutoController {
    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @PostMapping
    public ResponseEntity<DadosProduto> cadastrar(@RequestBody @Valid DadosCadastroProduto dto,
            @AuthenticationPrincipal Usuario logado, UriComponentsBuilder uriBuilder) {
        var produto = produtoService.cadastrar(dto, logado);
        URI uri = uriBuilder
                .path("/produto/{id}")
                .buildAndExpand(produto.id())
                .toUri();
        return ResponseEntity.created(uri).body(produto);
    }

    @GetMapping
    public Page<DadosProduto> buscarTodos(@PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return produtoService.buscarTodos(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DadosProduto> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(produtoService.buscarPorId(id));
    }

    @GetMapping("/vendedor/{id}")
    public Page<DadosProduto> buscarPorVendedor(@PathVariable Long id, @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return produtoService.buscarPorVendedor(id, pageable);
    }

    @PutMapping
    public ResponseEntity<DadosProduto> atualizar(@RequestBody @Valid DadosAtualizarProduto dto, @AuthenticationPrincipal Usuario logado) {
        return ResponseEntity.ok(produtoService.atualizar(dto, logado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletar(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        produtoService.deletar(id, usuario);
        return ResponseEntity.ok("Produto deletado com sucesso!");
    }

    @PatchMapping("/{id}")
    public ResponseEntity<String> reativar(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        produtoService.reativar(id, usuario);
        return ResponseEntity.ok("Produto reativado com sucesso!");
    }
}
