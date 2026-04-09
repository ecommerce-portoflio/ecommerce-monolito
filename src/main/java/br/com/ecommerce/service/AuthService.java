package br.com.ecommerce.service;

import br.com.ecommerce.model.auth.DadosLogin;
import br.com.ecommerce.model.usuario.Usuario;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AuthService(AuthenticationManager authenticationManager, TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    public String login(DadosLogin dadosLogin) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(dadosLogin.email(), dadosLogin.senha());
        var authentication = authenticationManager.authenticate(authenticationToken);
        var usuario = (Usuario) authentication.getPrincipal();

        return tokenService.gerarToken(usuario);
    }
}
