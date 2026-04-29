package br.com.ecommerce.controller;

import br.com.ecommerce.model.avaliacao.Avaliacao;
import br.com.ecommerce.model.avaliacao.DadosCadastroAvaliacao;
import br.com.ecommerce.model.produto.DadosCadastroProduto;
import br.com.ecommerce.model.produto.Produto;
import br.com.ecommerce.model.usuario.DadosCadastroUsuario;
import br.com.ecommerce.model.usuario.TipoPessoa;
import br.com.ecommerce.model.usuario.Usuario;
import br.com.ecommerce.repository.AvaliacaoRepository;
import br.com.ecommerce.repository.ProdutoRepository;
import br.com.ecommerce.repository.UsuarioRepository;
import br.com.ecommerce.service.TokenService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AvaliacaoControllerTest extends AbstractIntegrationTest{
    private final AvaliacaoRepository avaliacaoRepository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;

    private final PasswordEncoder encoder;
    private final TokenService tokenService;

    private Long idProduto;
    private Double notaJaAvaliada;

    @Autowired
    public AvaliacaoControllerTest(AvaliacaoRepository avaliacaoRepository, ProdutoRepository produtoRepository, UsuarioRepository usuarioRepository, PasswordEncoder encoder, TokenService tokenService) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.produtoRepository = produtoRepository;
        this.usuarioRepository = usuarioRepository;
        this.encoder = encoder;
        this.tokenService = tokenService;
    }

    @BeforeEach
    void limpaAvaliacoes() {
        avaliacaoRepository.deleteAll();
        produtoRepository.deleteAll();
        usuarioRepository.deleteAll();

        var usuario1 = new Usuario(new DadosCadastroUsuario("Nome 1", "email1@email.com", "Senha_123",
                "11 999999999", "123.456.789-00", TipoPessoa.PESSOA_FISICA), encoder.encode("Senha_123"));
        var usuario2 = new Usuario(new DadosCadastroUsuario("Nome 1", "email2@email.com", "Senha_123",
                "11 999999998", "123.456.789-99", TipoPessoa.PESSOA_FISICA), encoder.encode("Senha_123"));
        usuarioRepository.save(usuario1);
        usuarioRepository.save(usuario2);

        var produto1 = new Produto(new DadosCadastroProduto("Nome do Produto", "Descrição do produto",
                1000, BigDecimal.TEN), usuario1);
        idProduto = produtoRepository.save(produto1).getId();

        Avaliacao avaliacao = new Avaliacao(usuario1, 5.0, produto1);
        produto1.avaliar(avaliacao);
        produtoRepository.save(produto1);
        notaJaAvaliada = avaliacao.getNota();
    }

    @Test
    void deveAvaliarProduto() {
        var usuario = usuarioRepository.findByEmailIgnoreCase("email2@email.com").orElseThrow();
        var dto = new DadosCadastroAvaliacao(idProduto, 1.0);
        String token = tokenService.gerarToken(usuario);

        var response = postParaCadastrarAvaliacao(token, dto);

        assertEquals(200, response.statusCode());
        var produto = produtoRepository.findById(idProduto).orElseThrow();
        Double mediaEsperada = (notaJaAvaliada + dto.nota()) / 2;
        assertEquals(2, produto.getQuantidadeAvaliacoes());
        assertEquals(mediaEsperada, produto.getMediaAvaliacoes());
    }

    @Test
    void naoDeveAvaliarProdutoComNotaMaiorQue5() {
        var usuario = usuarioRepository.findByEmailIgnoreCase("email2@email.com").orElseThrow();
        var dto = new DadosCadastroAvaliacao(idProduto, 6.0);
        String token = tokenService.gerarToken(usuario);

        var response = postParaCadastrarAvaliacao(token, dto);

        assertEquals(400, response.statusCode());
        var produto = produtoRepository.findById(idProduto).orElseThrow();
        assertEquals(1, produto.getQuantidadeAvaliacoes());
        assertEquals(notaJaAvaliada, produto.getMediaAvaliacoes());
    }

    @Test
    void naoDeveAvaliarProdutoComNotaMenorQue0() {
        var usuario = usuarioRepository.findByEmailIgnoreCase("email2@email.com").orElseThrow();
        var dto = new DadosCadastroAvaliacao(idProduto, -1.0);
        String token = tokenService.gerarToken(usuario);

        var response = postParaCadastrarAvaliacao(token, dto);

        assertEquals(400, response.statusCode());
        var produto = produtoRepository.findById(idProduto).orElseThrow();
        assertEquals(1, produto.getQuantidadeAvaliacoes());
        assertEquals(notaJaAvaliada, produto.getMediaAvaliacoes());
    }

    @Test
    void deveAlterarAvaliacaoDeProdutoJaAvaliadoPeloUsuario() {
        var usuario = usuarioRepository.findByEmailIgnoreCase("email2@email.com").orElseThrow();
        var dto = new DadosCadastroAvaliacao(idProduto, 3.0);
        String token = tokenService.gerarToken(usuario);

        postParaCadastrarAvaliacao(token, dto); // Cadastro prévio de uma avaliação feita pelo usuário

        dto = new DadosCadastroAvaliacao(idProduto, 5.0);
        var response = postParaCadastrarAvaliacao(token, dto);

        assertEquals(200, response.statusCode());
        var produto = produtoRepository.findById(idProduto).orElseThrow();
        double novaMediaEsperada = (notaJaAvaliada + dto.nota()) / 2;
        assertEquals(novaMediaEsperada, produto.getMediaAvaliacoes());
        assertEquals(2, produto.getQuantidadeAvaliacoes());
        var avaliacao = avaliacaoRepository.findByAvaliadorAndProduto(usuario, produto).orElseThrow();
        assertEquals(dto.nota(), avaliacao.getNota());
    }


    @Test
    void naoDeveAvaliarProdutoNaoEncontrado() {
        var usuario = usuarioRepository.findByEmailIgnoreCase("email2@email.com").orElseThrow();
        var dto = new DadosCadastroAvaliacao(1000L, 3.0);
        String token = tokenService.gerarToken(usuario);

        var response = postParaCadastrarAvaliacao(token, dto);

        assertEquals(400, response.statusCode());
    }

    @Test
    void deveRemoverAvaliacaoDeProduto() {
        var usuario = usuarioRepository.findByEmailIgnoreCase("email2@email.com").orElseThrow();
        var dto = new DadosCadastroAvaliacao(idProduto, 3.0);
        String token = tokenService.gerarToken(usuario);

        postParaCadastrarAvaliacao(token, dto); // Cadastro prévio de uma avaliação feita pelo usuário

        var response = deleteParaDeletarAvaliacao(token, dto.idProduto());

        assertEquals(200, response.statusCode());
        var produto = produtoRepository.findById(idProduto).orElseThrow();
        assertEquals(1, produto.getQuantidadeAvaliacoes());
        assertEquals(notaJaAvaliada, produto.getMediaAvaliacoes());
        assertTrue(avaliacaoRepository.findByAvaliadorAndProduto(usuario, produto).isEmpty());
    }

    @Test
    void deveLancarExcecaoAoTentarRemoverAvaliacaoDeProdutoNaoAvaliado() {
        var usuario = usuarioRepository.findByEmailIgnoreCase("email2@email.com").orElseThrow();
        String token = tokenService.gerarToken(usuario);

        var response = deleteParaDeletarAvaliacao(token, idProduto);

        assertEquals(400, response.statusCode());
    }

    private static Response deleteParaDeletarAvaliacao(String token, Long idProduto) {
        return RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + token)
                .when()
                    .delete("/avaliacao/" + idProduto)
                .then()
                    .log().all()
                    .extract().response();
    }

    private static Response postParaCadastrarAvaliacao(String token, DadosCadastroAvaliacao dto) {
        return RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + token)
                    .body(dto)
                .when()
                    .post("/avaliacao")
                .then()
                    .log().all()
                    .extract().response();
    }
}
