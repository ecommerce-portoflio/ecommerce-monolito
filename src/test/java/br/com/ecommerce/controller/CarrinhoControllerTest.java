package br.com.ecommerce.controller;

import br.com.ecommerce.model.carrinho.DadosCadastroProdutoCarrinho;
import br.com.ecommerce.model.produto.DadosCadastroProduto;
import br.com.ecommerce.model.produto.Produto;
import br.com.ecommerce.model.usuario.DadosCadastroUsuario;
import br.com.ecommerce.model.usuario.TipoPessoa;
import br.com.ecommerce.model.usuario.Usuario;
import br.com.ecommerce.repository.CarrinhoRepository;
import br.com.ecommerce.repository.ProdutoCarrinhoRepository;
import br.com.ecommerce.repository.ProdutoRepository;
import br.com.ecommerce.repository.UsuarioRepository;
import br.com.ecommerce.service.TokenService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CarrinhoControllerTest extends AbstractIntegrationTest{
    private final UsuarioRepository usuarioRepository;
    private final CarrinhoRepository carrinhoRepository;
    private final ProdutoCarrinhoRepository produtoCarrinhoRepository;
    private final ProdutoRepository produtoRepository;

    private final PasswordEncoder encoder;
    private final TokenService tokenService;

    private Long idProduto1;
    private Long idProduto2;

    @Autowired
    public CarrinhoControllerTest(UsuarioRepository usuarioRepository, CarrinhoRepository carrinhoRepository, ProdutoCarrinhoRepository produtoCarrinhoRepository, ProdutoRepository produtoRepository, PasswordEncoder encoder, TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.carrinhoRepository = carrinhoRepository;
        this.produtoCarrinhoRepository = produtoCarrinhoRepository;
        this.produtoRepository = produtoRepository;
        this.encoder = encoder;
        this.tokenService = tokenService;
    }

    @BeforeEach
    void populaDados() {
        var usuario1 = new Usuario(new DadosCadastroUsuario("Nome 1", "email1@email.com", "Senha_123",
                "11 999999999", "123.456.789-00", TipoPessoa.PESSOA_FISICA), encoder.encode("Senha_123"));
        var usuario2 = new Usuario(new DadosCadastroUsuario("Nome 1", "email2@email.com", "Senha_123",
                "11 999999998", "123.456.789-99", TipoPessoa.PESSOA_FISICA), encoder.encode("Senha_123"));
        var usuario3 = new Usuario(new DadosCadastroUsuario("Nome 1", "email3@email.com", "Senha_123",
                "11 999999997", "123.456.789-22", TipoPessoa.PESSOA_FISICA), encoder.encode("Senha_123"));
        usuarioRepository.save(usuario1);
        usuarioRepository.save(usuario2);
        usuarioRepository.save(usuario3);

        var produto1 = new Produto(new DadosCadastroProduto("Nome do Produto", "Descrição do produto",
                1000, BigDecimal.TEN), usuario1);
        var produto2 = new Produto(new DadosCadastroProduto("Nome do Produto", "Descrição do produto",
                1000, BigDecimal.TEN), usuario2);
        idProduto1 = produtoRepository.save(produto1).getId();
        idProduto2 = produtoRepository.save(produto2).getId();
    }

    @Test
    void deveAdicionarProdutoAoCarrinho() {
        var usuario = usuarioRepository.findByEmailIgnoreCase("email3@email.com").orElseThrow();
        String token = tokenService.gerarToken(usuario);
        var produto1 = produtoRepository.findById(idProduto1).orElseThrow();
        var produto2 = produtoRepository.findById(idProduto2).orElseThrow();
        DadosCadastroProdutoCarrinho dto1 = new DadosCadastroProdutoCarrinho(idProduto1, 5);
        DadosCadastroProdutoCarrinho dto2 = new DadosCadastroProdutoCarrinho(idProduto2, 5);

        var response1 = RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + token)
                    .body(dto1)
                .when()
                    .post("/carrinho")
                .then()
                    .log().all()
                    .extract().response();

        var response2 = RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + token)
                    .body(dto2)
                .when()
                    .post("/carrinho")
                .then()
                    .log().all()
                    .extract().response();

        BigDecimal valorTotalEsperado = produto1.getValor().multiply(BigDecimal.valueOf(dto1.quantidade()))
                        .add(produto2.getValor().multiply(BigDecimal.valueOf(dto2.quantidade())));
        assertEquals(200, response1.statusCode());
        assertEquals(200, response2.statusCode());
        var carrinho = carrinhoRepository.findByUsuario(usuario);
        assertEquals(2, carrinho.getProdutos().size());
        assertEquals(0, carrinho.getTotal().compareTo(valorTotalEsperado));
    }

    @Test
    void deveBuscarCarrinho() {
        var usuario = usuarioRepository.findByEmailIgnoreCase("email3@email.com").orElseThrow();
        String token = tokenService.gerarToken(usuario);
        var produto1 = produtoRepository.findById(idProduto1).orElseThrow();
        DadosCadastroProdutoCarrinho dto1 = new DadosCadastroProdutoCarrinho(idProduto1, 5);

        RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + token)
                    .body(dto1)
                .when()
                    .post("/carrinho")
                .then()
                    .log().all();

        var response = RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + token)
                .when()
                    .get("/carrinho")
                .then()
                    .log().all()
                    .extract().response();

        BigDecimal totalEsperado = produto1.getValor().multiply(BigDecimal.valueOf(dto1.quantidade()));
        assertEquals(200, response.statusCode());
        assertEquals(0, totalEsperado.compareTo(BigDecimal.valueOf(response.jsonPath().getDouble("total"))));
        assertEquals(1, response.jsonPath().getList("produtos").size());
    }

    @Test
    void deveRemoverProdutoDoCarrinho() {
        var usuario = usuarioRepository.findByEmailIgnoreCase("email3@email.com").orElseThrow();
        String token = tokenService.gerarToken(usuario);
        var produto1 = produtoRepository.findById(idProduto1).orElseThrow();
        DadosCadastroProdutoCarrinho dto1 = new DadosCadastroProdutoCarrinho(idProduto1, 5);

        RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + token)
                    .body(dto1)
                .when()
                    .post("/carrinho")
                .then()
                    .log().all();

        var response = RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + token)
                .when()
                    .delete("/carrinho/" + produto1.getId())
                .then()
                    .log().all()
                    .extract().response();

        assertEquals(200, response.statusCode());
        var carrinho = carrinhoRepository.findByUsuario(usuario);
        assertEquals(BigDecimal.ZERO.doubleValue(), carrinho.getTotal().doubleValue());
        assertEquals(0, carrinho.getProdutos().size());
    }

    @Test
    void deveEsvaziarCarrinho() {
        var usuario = usuarioRepository.findByEmailIgnoreCase("email3@email.com").orElseThrow();
        String token = tokenService.gerarToken(usuario);
        DadosCadastroProdutoCarrinho dto1 = new DadosCadastroProdutoCarrinho(idProduto1, 5);
        DadosCadastroProdutoCarrinho dto2 = new DadosCadastroProdutoCarrinho(idProduto2, 5);

        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(dto1)
                .when()
                .post("/carrinho")
                .then()
                .log().all();

        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(dto2)
                .when()
                .post("/carrinho")
                .then()
                .log().all();

        var response = RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + token)
                .when()
                    .delete("/carrinho/todos")
                .then()
                    .log().all()
                    .extract().response();

        assertEquals(200, response.statusCode());
        var carrinho = carrinhoRepository.findByUsuario(usuario);
        assertEquals(BigDecimal.ZERO.doubleValue(), carrinho.getTotal().doubleValue());
        assertEquals(0, carrinho.getProdutos().size());
    }
}
