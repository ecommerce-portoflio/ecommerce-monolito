package br.com.ecommerce.controller;

import br.com.ecommerce.model.auth.DadosLogin;
import br.com.ecommerce.model.usuario.Usuario;
import br.com.ecommerce.service.AuthService;
import br.com.ecommerce.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody DadosLogin dadosLogin) {
        String token = authService.login(dadosLogin);
        return ResponseEntity.ok(token);
    }
}
