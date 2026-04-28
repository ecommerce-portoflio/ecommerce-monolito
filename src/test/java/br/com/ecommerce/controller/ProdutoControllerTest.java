package br.com.ecommerce.controller;

import br.com.ecommerce.model.produto.DadosAtualizarProduto;
import br.com.ecommerce.model.produto.DadosCadastroProduto;
import br.com.ecommerce.model.produto.Produto;
import br.com.ecommerce.model.usuario.DadosCadastroUsuario;
import br.com.ecommerce.model.usuario.TipoPessoa;
import br.com.ecommerce.model.usuario.Usuario;
import br.com.ecommerce.repository.ProdutoRepository;
import br.com.ecommerce.repository.UsuarioRepository;
import br.com.ecommerce.service.TokenService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProdutoControllerTest extends AbstractIntegrationTest {
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder encoder;
    private final TokenService tokenService;

    @Autowired
    public ProdutoControllerTest(ProdutoRepository produtoRepository, UsuarioRepository usuarioRepository, PasswordEncoder encoder, TokenService tokenService) {
        this.produtoRepository = produtoRepository;
        this.usuarioRepository = usuarioRepository;
        this.encoder = encoder;
        this.tokenService = tokenService;
    }

    @BeforeAll
    void criaUsuarios() {
        var usuario1 = new Usuario(new DadosCadastroUsuario("Nome 1", "email1@email.com", "Senha_123",
                "11 999999999", "123.456.789-00", TipoPessoa.PESSOA_FISICA), encoder.encode("Senha_123"));
        var usuario2 = new Usuario(new DadosCadastroUsuario("Nome 1", "email2@email.com", "Senha_123",
                "11 999999998", "123.456.789-99", TipoPessoa.PESSOA_FISICA), encoder.encode("Senha_123"));
        usuarioRepository.save(usuario1);
        usuarioRepository.save(usuario2);
    }

    @BeforeEach
    void limpaBanco() {
        produtoRepository.deleteAll();
    }

    @AfterAll
    void limpaDados() {
        limpaBanco();
        usuarioRepository.deleteAll();
    }

    @Test
    void deveCadastrarProduto() {
        DadosCadastroProduto dto = new DadosCadastroProduto("Novo Produto", "Produto muito legal", 20,
                BigDecimal.valueOf(12.60));
        Usuario usuarioLogado = usuarioRepository.findByEmailIgnoreCase("email1@email.com").get();
        String token = tokenService.gerarToken(usuarioLogado);

        var response = RestAssured
                .given()
                .log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(dto)
                .when()
                .post("/produto")
                .then()
                .log().all()
                .extract().response();

        assertEquals(201, response.statusCode());
        assertEquals(dto.nome(), response.jsonPath().getString("nome"));
        assertEquals(dto.descricao(), response.jsonPath().getString("descricao"));
        assertEquals(dto.quantidadeEstoque(), response.jsonPath().getInt("quantidadeEstoque"));
        assertEquals(dto.valor().doubleValue(), response.jsonPath().getDouble("valor"));
        assertEquals(usuarioLogado.getId(), response.jsonPath().getLong("idVendedor"));
    }

    @Test
    void deveBuscarTodos() {
        Usuario usuarioLogado = usuarioRepository.findByEmailIgnoreCase("email1@email.com").get();
        String token = tokenService.gerarToken(usuarioLogado);
        for (int i = 0; i < 15; i++) {
            produtoRepository.save(criaProduto(usuarioLogado));
        }

        var response = RestAssured
                .given()
                .log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/produto")
                .then()
                .log().all()
                .extract().response();

        assertEquals(15, response.jsonPath().getInt("totalElements"));
    }

    @Test
    void deveBuscarPorVendedor() {
        Usuario vendedor1 = usuarioRepository.findByEmailIgnoreCase("email1@email.com").get();
        Usuario vendedor2 = usuarioRepository.findByEmailIgnoreCase("email2@email.com").get();

        for (int i = 0; i < 5; i++)
            produtoRepository.save(criaProduto(vendedor2));
        produtoRepository.save(criaProduto(vendedor1));

        String token = tokenService.gerarToken(vendedor1);

        var response = RestAssured
                .given()
                    .log().all()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + token)
                .when()
                    .get("/produto/vendedor/" + vendedor1.getId())
                .then()
                    .log().all()
                    .extract().response();

        assertEquals(1, response.jsonPath().getInt("totalElements"));
        assertEquals(vendedor1.getId(), response.jsonPath().getLong("content[0].idVendedor"));
    }

    @Test
    void deveAtualizarProduto() {
        var usuarioVendedor = usuarioRepository.findByEmailIgnoreCase("email1@email.com").get();
        var produto = produtoRepository.save(criaProduto(usuarioVendedor));
        DadosAtualizarProduto dto = new DadosAtualizarProduto(produto.getId(), "Novo nome", "Nova descrição",
                200, null);
        String token = tokenService.gerarToken(usuarioVendedor);

        var response = RestAssured
                .given()
                    .log().all()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + token)
                    .body(dto)
                .when()
                    .put("/produto")
                .then()
                    .log().all()
                    .extract().response();

        assertEquals(200, response.statusCode());
        assertEquals(dto.nome(), response.jsonPath().getString("nome"));
        assertEquals(dto.descricao(), response.jsonPath().getString("descricao"));
        assertEquals(dto.quantidadeEstoque(), response.jsonPath().getInt("quantidadeEstoque"));
        assertEquals(produto.getValor().doubleValue(), response.jsonPath().getDouble("valor")); // Valor não foi alterado
    }

    @Test
    void naoDeveAtualizarProdutoQuandoUsuarioNaoForOVendedor() {
        var usuarioVendedor = usuarioRepository.findByEmailIgnoreCase("email1@email.com").get();
        var usuarioLogado = usuarioRepository.findByEmailIgnoreCase("email2@email.com").get();
        var produto = produtoRepository.save(criaProduto(usuarioVendedor));
        DadosAtualizarProduto dto = new DadosAtualizarProduto(produto.getId(), "Novo nome", "Nova descrição",
                200, null);
        String token = tokenService.gerarToken(usuarioLogado);

        var response = RestAssured
                .given()
                    .log().all()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + token)
                    .body(dto)
                .when()
                    .put("/produto")
                .then()
                    .log().all()
                    .extract().response();

        assertEquals(400, response.statusCode());
        assertEquals("Erro de regra de negócio", response.jsonPath().getString("error"));
        assertEquals("Você não tem permissão para atualizar este produto!", response.jsonPath().getString("message"));
    }

    @Test
    void deveDeletarProduto() {
        var usuarioVendedor = usuarioRepository.findByEmailIgnoreCase("email1@email.com").get();
        var produto = produtoRepository.save(criaProduto(usuarioVendedor));
        String token = tokenService.gerarToken(usuarioVendedor);

        var response = RestAssured
                .given()
                    .log().all()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + token)
                .when()
                    .delete("/produto/" + produto.getId())
                .then()
                    .log().all()
                    .extract().response();

        assertEquals(200, response.statusCode());
        assertEquals("Produto deletado com sucesso!", response.asString());
        var produtoDB = produtoRepository.findById(produto.getId()).get();
        assertFalse(produtoDB.isAtivo());
    }

    @Test
    void naoDeveDeletarProdutoQuandoUsuarioNaoForODono() {
        var usuarioVendedor = usuarioRepository.findByEmailIgnoreCase("email1@email.com").get();
        var usuarioLogado = usuarioRepository.findByEmailIgnoreCase("email2@email.com").get();
        var produto = criaProduto(usuarioVendedor);
        var produtoDB = produtoRepository.save(produto);
        String token = tokenService.gerarToken(usuarioLogado);

        var response = RestAssured
                .given()
                    .log().all()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + token)
                .when()
                    .delete("/produto/" + produtoDB.getId())
                .then()
                    .log().all()
                    .extract().response();

        assertEquals(400, response.statusCode());
        assertEquals("Erro de regra de negócio", response.jsonPath().getString("error"));
        assertEquals("Você não tem permissão para alterar este produto!", response.jsonPath().getString("message"));
        produtoDB = produtoRepository.findById(produtoDB.getId()).get();
        assertTrue(produtoDB.isAtivo());
    }

    @Test
    void deveReativarProduto() {
        var usuarioVendedor = usuarioRepository.findByEmailIgnoreCase("email1@email.com").get();
        var produto = criaProduto(usuarioVendedor);
        produto.setAtivo(false);
        var produtoDB = produtoRepository.save(produto);
        String token = tokenService.gerarToken(usuarioVendedor);

        var response = RestAssured
                .given()
                    .log().all()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + token)
                .when()
                    .patch("/produto/" + produtoDB.getId())
                .then()
                    .log().all()
                    .extract().response();

        assertEquals(200, response.statusCode());
        assertEquals("Produto reativado com sucesso!", response.asString());
        produtoDB = produtoRepository.findById(produtoDB.getId()).get();
        assertTrue(produtoDB.isAtivo());
    }

    @Test
    void naoDeveReativarProdutoQuandoUsuarioNaoForODono() {
        var usuarioVendedor = usuarioRepository.findByEmailIgnoreCase("email1@email.com").get();
        var usuarioLogado = usuarioRepository.findByEmailIgnoreCase("email2@email.com").get();
        var produto = criaProduto(usuarioVendedor);
        produto.setAtivo(false);
        var produtoDB = produtoRepository.save(produto);
        String token = tokenService.gerarToken(usuarioLogado);

        var response = RestAssured
                .given()
                    .log().all()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + token)
                .when()
                    .patch("/produto/" + produtoDB.getId())
                .then()
                    .log().all()
                    .extract().response();

        assertEquals(400, response.statusCode());
        assertEquals("Erro de regra de negócio", response.jsonPath().getString("error"));
        assertEquals("Você não tem permissão para alterar este produto!", response.jsonPath().getString("message"));
        produtoDB = produtoRepository.findById(produtoDB.getId()).get();
        assertFalse(produtoDB.isAtivo());
    }

    Produto criaProduto(Usuario usuario) {
        return new Produto(criaDtoCadastroProduto(), usuario);
    }

    DadosCadastroProduto criaDtoCadastroProduto() {
        return new DadosCadastroProduto("Nome do Produto", "Descrição do produto",
                100, BigDecimal.TEN);
    }
}
