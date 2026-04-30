package br.com.ecommerce.controller;

import br.com.ecommerce.model.usuario.DadosAtualizarUsuario;
import br.com.ecommerce.model.usuario.DadosCadastroUsuario;
import br.com.ecommerce.model.usuario.TipoPessoa;
import br.com.ecommerce.model.usuario.Usuario;
import br.com.ecommerce.repository.UsuarioRepository;
import br.com.ecommerce.service.TokenService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class UsuarioControllerTest extends AbstractIntegrationTest {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Autowired
    public UsuarioControllerTest(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @WithAnonymousUser
    @Test
    void deveRegistrarUsuario() {
        DadosCadastroUsuario dto = new DadosCadastroUsuario("Usuário nome", "email@email.com", "Senha_9090",
                "11 900000000", "123.456.789-00", TipoPessoa.PESSOA_FISICA);

        var response = RestAssured
                .given()
                    .log().all()
                    .contentType(ContentType.JSON)
                    .body(dto)
                .when()
                    .post("/usuario")
                .then()
                    .log().all()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(201);
        assertEquals(response.jsonPath().getString("nome"), dto.nome());
        assertEquals(response.jsonPath().getString("email"), dto.email());
        assertEquals(response.jsonPath().getString("telefone"), dto.telefone());
    }

    @WithAnonymousUser
    @Test
    void naoDeveRegistrarUsuarioComEmailJaCadastrado() {
        DadosCadastroUsuario dtoUsuarioExistente = new DadosCadastroUsuario("Usuário nome", "email@email.com",
                "Senha_9090","11 900000000", "123.456.789-00", TipoPessoa.PESSOA_FISICA);
        Usuario usuarioExistente = criaUsuario(dtoUsuarioExistente);
        usuarioRepository.save(usuarioExistente);

        DadosCadastroUsuario dtoNovoUsuario = new DadosCadastroUsuario("Novo nome", "email@email.com",
                "Senha_9090","11 900000001", "123.456.789-01", TipoPessoa.PESSOA_FISICA);

        var response = RestAssured
                .given()
                    .log().all()
                    .contentType(ContentType.JSON)
                    .body(dtoNovoUsuario)
                .when()
                    .post("/usuario")
                .then()
                    .log().all()
                    .extract().response();

        assertEquals(400, response.statusCode());
        assertEquals("Erro de regra de negócio", response.jsonPath().getString("error"));
        assertEquals("Um ou mais dados já foram cadastrados para outro usuário!", response.jsonPath().getString("message"));
    }

    @Test
    void deveAtualizarUsuario() {
        DadosCadastroUsuario dto = new DadosCadastroUsuario("Usuário nome", "email@email.com",
                "Senha_9090","11 900000000", "123.456.789-00", TipoPessoa.PESSOA_FISICA);
        Usuario usuario = criaUsuario(dto);
        usuarioRepository.save(usuario);
        String token = tokenService.gerarToken(usuario);

        DadosAtualizarUsuario dtoAtualizar = new DadosAtualizarUsuario("Novo nome", null, null, "99 999999999");

        var response = RestAssured
                .given()
                    .log().all()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + token)
                    .body(dtoAtualizar)
                .when()
                    .put("/usuario")
                .then()
                    .log().all()
                    .extract().response();

        assertEquals(200, response.statusCode());
        assertEquals("Novo nome", response.jsonPath().getString("nome"));
        assertEquals("email@email.com", response.jsonPath().getString("email")); // O email deve manter o mesmo
        assertEquals("99 999999999", response.jsonPath().getString("telefone"));
    }

    @Test
    void deveDeletarUsuario() {
        DadosCadastroUsuario dtoUsuarioDeletado = new DadosCadastroUsuario("Usuário nome", "email@email.com",
                "Senha_9090","11 900000000", "123.456.789-00", TipoPessoa.PESSOA_FISICA);
        Usuario usuarioDeletado = criaUsuario(dtoUsuarioDeletado);
        Usuario usuarioDeletadoBD = usuarioRepository.save(usuarioDeletado);

        DadosCadastroUsuario dtoUsuarioAdmin = new DadosCadastroUsuario("Usuário ADMIN", "admin@email.com",
                "Senha_9090","11 900000001", "123.456.789-10", TipoPessoa.PESSOA_FISICA);
        Usuario usuarioAdmin = criaUsuario(dtoUsuarioAdmin);
        usuarioAdmin.setRole("ROLE_ADMIN");
        usuarioRepository.save(usuarioAdmin);

        String token = tokenService.gerarToken(usuarioAdmin);

        var response = RestAssured
                .given()
                    .log().all()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + token)
                .when()
                    .delete("/usuario/" + usuarioDeletadoBD.getId())
                .then()
                    .log().all()
                    .extract().response();

        assertEquals(200, response.statusCode());
        assertEquals("Usuário deletado com sucesso!", response.asString());
        var usuario = usuarioRepository.findById(usuarioDeletadoBD.getId());
        assertFalse(usuario.get().isAtivo());
    }

    void naoDeveAtualizarUsuarioSeNaoForAdmin() {
        DadosCadastroUsuario dtoUsuarioDeletado = new DadosCadastroUsuario("Usuário nome", "email@email.com",
                "Senha_9090","11 900000000", "123.456.789-00", TipoPessoa.PESSOA_FISICA);
        Usuario usuarioDeletado = criaUsuario(dtoUsuarioDeletado);
        Usuario usuarioDeletadoBD = usuarioRepository.save(usuarioDeletado);

        DadosCadastroUsuario dtoUsuarioComum = new DadosCadastroUsuario("Usuário ADMIN", "admin@email.com",
                "Senha_9090","11 900000001", "123.456.789-10", TipoPessoa.PESSOA_FISICA);
        Usuario usuarioComum = criaUsuario(dtoUsuarioComum);
        usuarioRepository.save(usuarioComum);

        String token = tokenService.gerarToken(usuarioComum);

        var response = RestAssured
                .given()
                .log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/usuario/" + usuarioDeletadoBD.getId())
                .then()
                .log().all()
                .extract().response();

        assertEquals(403, response.statusCode());
        assertEquals("Acesso negado", response.jsonPath().getString("message"));
        assertEquals("Você não tem acesso a este recurso", response.jsonPath().getString("error"));
        var usuario = usuarioRepository.findById(usuarioDeletadoBD.getId());
        assertTrue(usuario.get().isAtivo());
    }

    Usuario criaUsuario(DadosCadastroUsuario dto) {
        return new Usuario(dto, passwordEncoder.encode(dto.senha()));
    }
}
