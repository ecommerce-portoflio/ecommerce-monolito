package br.com.ecommerce.infra.security;

import br.com.ecommerce.model.usuario.Usuario;
import br.com.ecommerce.repository.UsuarioRepository;
import br.com.ecommerce.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class FiltroTokenAcesso extends OncePerRequestFilter {
    private final UsuarioRepository usuarioRepository;
    private final TokenService tokenService;

    public FiltroTokenAcesso(UsuarioRepository usuarioRepository, TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Recuperar o token da requisição
        String token = recuperarToken(request);

        if (token != null) {
            // Validação do token
            String email = tokenService.verificarToken(token);
            Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElseThrow();
            Authentication authentication = new UsernamePasswordAuthenticationToken(usuario, null,
                    usuario.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String recuperarToken(HttpServletRequest request) {
        var authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null) {
            return authorizationHeader.replace("Bearer ", "");
        }
        return null;
    }
}

