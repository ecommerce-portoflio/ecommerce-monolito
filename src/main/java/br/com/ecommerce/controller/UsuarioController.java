package br.com.ecommerce.controller;

import br.com.ecommerce.model.usuario.DadosCadastro;
import br.com.ecommerce.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody DadosCadastro dadosCadastro) {
        var usuario = usuarioService.cadastrar(dadosCadastro);
        return ResponseEntity.ok(usuario);
    }
}
