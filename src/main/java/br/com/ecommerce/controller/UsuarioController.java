package br.com.ecommerce.controller;

import br.com.ecommerce.model.usuario.DadosAtualizarUsuario;
import br.com.ecommerce.model.usuario.DadosCadastro;
import br.com.ecommerce.model.usuario.Usuario;
import br.com.ecommerce.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody @Valid DadosCadastro dadosCadastro, UriComponentsBuilder uriBuilder) {
        var usuario = usuarioService.cadastrar(dadosCadastro);
        URI uri = uriBuilder
                .path("/usuario/{id}")
                .buildAndExpand(usuario.id())
                .toUri();

        return ResponseEntity
                .created(uri)
                .body(usuario);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        var usuario = usuarioService.buscarPorId(id);
        return ResponseEntity.ok(usuario);
    }

    @PutMapping
    public ResponseEntity<?> atualizar(@RequestBody DadosAtualizarUsuario dto, @AuthenticationPrincipal Usuario logado) {
        var novoUsuario = usuarioService.atualizar(dto, logado);
        return ResponseEntity.ok(novoUsuario);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deletar(@PathVariable Long id) {
        usuarioService.deletar(id);
        return "Usuário deletado com sucesso!";
    }
}
