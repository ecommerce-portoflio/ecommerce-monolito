package br.com.ecommerce.service;

import br.com.ecommerce.model.usuario.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;

@Component
public class TokenService {
    @Value("${app.secret}")
    private String SECRET;

    public String gerarToken(Usuario usuario) {
        var algoritmo = Algorithm.HMAC256(SECRET);
        return JWT
                .create()
                .withIssuer("E-commerce do Marcelo")
                .withSubject(usuario.getUsername())
                .withExpiresAt(dataExpiracao())
                .sign(algoritmo);
    }

    public String verificarToken(String tokenJWT) {
        var algoritmo = Algorithm.HMAC256(SECRET);
        return JWT
                .require(algoritmo)
                .withIssuer("E-commerce do Marcelo")
                .build()
                .verify(tokenJWT)
                .getSubject();
    }

    private Date dataExpiracao() {
        return Date.from(Instant.now().plusSeconds(60 * 30)); // 30 minutos de expiração
    }
}
