package br.com.ecommerce.controller;

import br.com.ecommerce.model.pedido.DadosCadastroPedido;
import br.com.ecommerce.model.pedido.Pedido;
import br.com.ecommerce.model.pedido.StatusPedido;
import br.com.ecommerce.model.produto.DadosCadastroProduto;
import br.com.ecommerce.model.produto.Produto;
import br.com.ecommerce.model.usuario.DadosCadastroUsuario;
import br.com.ecommerce.model.usuario.TipoPessoa;
import br.com.ecommerce.model.usuario.Usuario;
import br.com.ecommerce.repository.*;
import br.com.ecommerce.service.PedidoService;
import br.com.ecommerce.service.TokenService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PedidoControllerTest extends AbstractIntegrationTest{
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProdutoRepository produtoRepository;
    private final ProdutoPedidoRepository produtoPedidoRepository;
    private final CarrinhoRepository carrinhoRepository;
    private final ProdutoCarrinhoRepository produtoCarrinhoRepository;

    private final PedidoService pedidoService;

    private final PasswordEncoder encoder;
    private final TokenService tokenService;

    private Long idProduto1;
    private Long idProduto2;

    @Autowired
    public PedidoControllerTest(PedidoRepository pedidoRepository, UsuarioRepository usuarioRepository, ProdutoRepository produtoRepository, ProdutoPedidoRepository produtoPedidoRepository, CarrinhoRepository carrinhoRepository, ProdutoCarrinhoRepository produtoCarrinhoRepository, PedidoService pedidoService, PasswordEncoder encoder, TokenService tokenService) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.produtoRepository = produtoRepository;
        this.produtoPedidoRepository = produtoPedidoRepository;
        this.carrinhoRepository = carrinhoRepository;
        this.produtoCarrinhoRepository = produtoCarrinhoRepository;
        this.pedidoService = pedidoService;
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
    void deveFazerPedidoComProdutoUnico() {
        var produto = produtoRepository.findByIdAndAtivo(idProduto1, true).orElseThrow();
        DadosCadastroPedido dto = new DadosCadastroPedido(produto.getId(), 2);
        var usuario = usuarioRepository.findByEmailIgnoreCase("email3@email.com").orElseThrow();
        var token = tokenService.gerarToken(usuario);

        var response = RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + token)
                    .body(dto)
                .when()
                    .post("/pedido")
                .then()
                    .log().all()
                    .extract().response();

        BigDecimal valorEsperado = produto.getValor().multiply(BigDecimal.valueOf(dto.quantidade()));
        assertEquals(valorEsperado.doubleValue(), response.jsonPath().getDouble("valorTotal"));
        assertEquals(usuario.getId(), response.jsonPath().getLong("compradorId"));
        assertEquals(StatusPedido.AGUARDANDO_PAGAMENTO.toString(), response.jsonPath().getString("statusPedido"));
        assertEquals(1, response.jsonPath().getList("produtos").size());
    }

    @Test
    void naoDeveFazerPedidoComQuantidadeNula() {
        var produto = produtoRepository.findByIdAndAtivo(idProduto1, true).orElseThrow();
        DadosCadastroPedido dto = new DadosCadastroPedido(produto.getId(), null);
        var usuario = usuarioRepository.findByEmailIgnoreCase("email3@email.com").orElseThrow();
        var token = tokenService.gerarToken(usuario);

        var response = RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + token)
                    .body(dto)
                .when()
                    .post("/pedido")
                .then()
                    .log().all()
                    .extract().response();

        assertEquals(400, response.statusCode());
    }

    @Test
    void deveFazerPedidoAPartirDeUmCarrinho() {
        var usuario = usuarioRepository.findByEmailIgnoreCase("email3@email.com").orElseThrow();
        var carrinho = carrinhoRepository.findByUsuario(usuario);
        var produto1 = produtoRepository.findByIdAndAtivo(idProduto1, true).orElseThrow();
        var produto2 = produtoRepository.findByIdAndAtivo(idProduto2, true).orElseThrow();
        carrinho.adicionarProduto(produto1, 5);
        carrinho.adicionarProduto(produto2, 5);
        carrinhoRepository.save(carrinho);
        var token = tokenService.gerarToken(usuario);

        var response = RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + token)
                .when()
                    .post("/pedido/carrinho")
                .then()
                    .log().all()
                    .extract().response();

        assertEquals(201, response.statusCode());
        assertEquals(2, response.jsonPath().getList("produtos").size());
        carrinho = carrinhoRepository.findByUsuario(usuario);
        assertTrue(carrinho.getProdutos().isEmpty());
        assertEquals(0, carrinho.getTotal().compareTo(BigDecimal.ZERO));
    }

    @Test
    void deveFazerPedidoAPartirDeVariosProdutos() {
        var usuario = usuarioRepository.findByEmailIgnoreCase("email3@email.com").orElseThrow();
        var carrinho = carrinhoRepository.findByUsuario(usuario);
        var produto1 = produtoRepository.findByIdAndAtivo(idProduto1, true).orElseThrow();
        var produto2 = produtoRepository.findByIdAndAtivo(idProduto2, true).orElseThrow();
        carrinho.adicionarProduto(produto1, 5);
        carrinho.adicionarProduto(produto2, 5);
        carrinhoRepository.save(carrinho);

        List<DadosCadastroPedido> dto = new ArrayList<>();
        var dto1 = new DadosCadastroPedido(produto1.getId(), 5);
        var dto2 = new DadosCadastroPedido(produto2.getId(), 5);
        dto.add(dto1);
        dto.add(dto2);

        var token = tokenService.gerarToken(usuario);

        var response = RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + token)
                    .body(dto)
                .when()
                    .post("/pedido/varios")
                .then()
                    .log().all()
                    .extract().response();

        BigDecimal valorTotalEsperado = produto1.getValor().multiply(BigDecimal.valueOf(dto1.quantidade()))
                .add(produto2.getValor().multiply(BigDecimal.valueOf(dto2.quantidade())));
        assertEquals(201, response.statusCode());
        assertEquals(2, response.jsonPath().getList("produtos").size());
        carrinho = carrinhoRepository.findByUsuario(usuario);
        assertEquals(0, carrinho.getTotal().compareTo(BigDecimal.ZERO));
        assertEquals(valorTotalEsperado.doubleValue(), response.jsonPath().getDouble("valorTotal"));
    }

    @Test
    void devePagarPedido() {
        var usuario = usuarioRepository.findByEmailIgnoreCase("email3@email.com").orElseThrow();
        var produto1 = produtoRepository.findByIdAndAtivo(idProduto1, true).orElseThrow();
        var pedido = pedidoRepository.save(new Pedido(produto1, usuario, 5));
        var token = tokenService.gerarToken(usuario);

        var response = RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + token)
                .when()
                    .post("/pedido/pagar/" + pedido.getId())
                .then()
                    .log().all()
                    .extract().response();

        assertEquals(200, response.statusCode());
        pedido = pedidoRepository.findById(pedido.getId()).orElseThrow();
        assertEquals(StatusPedido.PAGO, pedido.getStatusPedido());
    }

    @Test
    void deveEntregarPedido() {
        var usuario = usuarioRepository.findByEmailIgnoreCase("email3@email.com").orElseThrow();
        var produto1 = produtoRepository.findByIdAndAtivo(idProduto1, true).orElseThrow();
        var pedido = new Pedido(produto1, usuario, 5);
        pedido.setStatusPedido(StatusPedido.PAGO);
        pedidoRepository.save(pedido);
        var token = tokenService.gerarToken(usuario);

        var response = RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + token)
                .when()
                    .post("/pedido/entregar/" + pedido.getId())
                .then()
                    .log().all()
                    .extract().response();

        assertEquals(200, response.statusCode());
        pedido = pedidoRepository.findById(pedido.getId()).orElseThrow();
        assertEquals(StatusPedido.ENTREGUE, pedido.getStatusPedido());
    }

    @Test
    void deveBuscarPedido() {
        var usuario = usuarioRepository.findByEmailIgnoreCase("email3@email.com").orElseThrow();
        var produto1 = produtoRepository.findByIdAndAtivo(idProduto1, true).orElseThrow();
        var pedido = pedidoRepository.save(new Pedido(produto1, usuario, 5));
        var token = tokenService.gerarToken(usuario);

        var response = RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + token)
                .when()
                    .get("/pedido/" + pedido.getId())
                .then()
                    .log().all()
                    .extract().response();

        assertEquals(200, response.statusCode());
        assertEquals(usuario.getId(), response.jsonPath().getLong("compradorId"));
        assertEquals(pedido.getProdutos().size(), response.jsonPath().getList("produtos").size());
    }

    @Test
    void naoDeveBuscarPedidoDeOutroUsuario() {
        var usuario = usuarioRepository.findByEmailIgnoreCase("email3@email.com").orElseThrow();
        var produto1 = produtoRepository.findByIdAndAtivo(idProduto1, true).orElseThrow();
        var pedido = pedidoRepository.save(new Pedido(produto1, usuario, 5));
        var usuario2 = usuarioRepository.findByEmailIgnoreCase("email2@email.com").orElseThrow();
        var token = tokenService.gerarToken(usuario2);

        var response = RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + token)
                .when()
                    .get("/pedido/" + pedido.getId())
                .then()
                    .log().all()
                    .extract().response();

        assertEquals(400, response.statusCode());
    }

    @Test
    void deveRetornarTodosOsPedidosDoUsuario() {
        var usuario = usuarioRepository.findByEmailIgnoreCase("email3@email.com").orElseThrow();
        var produto1 = produtoRepository.findByIdAndAtivo(idProduto1, true).orElseThrow();
        var pedido1 = pedidoRepository.save(new Pedido(produto1, usuario, 5));
        var pedido2 = pedidoRepository.save(new Pedido(produto1, usuario, 5));
        var pedido3 = pedidoRepository.save(new Pedido(produto1, usuario, 5));
        var token = tokenService.gerarToken(usuario);

        var response = RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + token)
                .when()
                    .get("/pedido/todos")
                .then()
                    .log().all()
                    .extract().response();

        assertEquals(200, response.statusCode());
        assertEquals(3, response.jsonPath().getList("content").size());
    }

    @Test
    void deveRetornarTodosOsPedidosVendidos() {
        var usuario = usuarioRepository.findByEmailIgnoreCase("email3@email.com").orElseThrow();
        var produto1 = produtoRepository.findByIdAndAtivo(idProduto1, true).orElseThrow();
        var pedido1 = pedidoRepository.save(new Pedido(produto1, usuario, 5));
        var pedido2 = pedidoRepository.save(new Pedido(produto1, usuario, 5));
        var pedido3 = pedidoRepository.save(new Pedido(produto1, usuario, 5));
        var vendedor = usuarioRepository.findByEmailIgnoreCase("email1@email.com").orElseThrow();
        var token = tokenService.gerarToken(vendedor);

        var response = RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + token)
                .when()
                    .get("/pedido/todos/vendidos")
                .then()
                    .log().all()
                    .extract().response();

        assertEquals(200, response.statusCode());
        assertEquals(3, response.jsonPath().getList("content").size());
    }

    @Test
    void deveCancelarPedido() {
        var usuario = usuarioRepository.findByEmailIgnoreCase("email3@email.com").orElseThrow();
        var produto1 = produtoRepository.findByIdAndAtivo(idProduto1, true).orElseThrow();
        int quantidadeEstoqueAntesDoPedido = produto1.getQuantidadeEstoque();
        DadosCadastroPedido dto = new DadosCadastroPedido(produto1.getId(), 2);
        var token = tokenService.gerarToken(usuario);

        var pedido = pedidoService.fazerPedidoProdutoUnico(dto, usuario);

        var response = RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + token)
                .when()
                    .delete("/pedido/" + pedido.id())
                .then()
                    .log().all()
                    .extract().response();

        assertEquals(200, response.statusCode());
        assertTrue(pedidoRepository.findById(pedido.id()).isEmpty());
        produto1 = produtoRepository.findById(produto1.getId()).orElseThrow();
        assertEquals(quantidadeEstoqueAntesDoPedido, produto1.getQuantidadeEstoque());
    }

    @Test
    void naoDeveCancelarPedidoDeOutroUsuario() {
        var usuario = usuarioRepository.findByEmailIgnoreCase("email3@email.com").orElseThrow();
        var produto1 = produtoRepository.findByIdAndAtivo(idProduto1, true).orElseThrow();
        DadosCadastroPedido dto = new DadosCadastroPedido(produto1.getId(), 2);
        var usuario2 = usuarioRepository.findByEmailIgnoreCase("email2@email.com").orElseThrow();
        var token = tokenService.gerarToken(usuario2);

        var pedido = pedidoService.fazerPedidoProdutoUnico(dto, usuario);

        var response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/pedido/" + pedido.id())
                .then()
                .log().all()
                .extract().response();

        assertEquals(400, response.statusCode());
        assertFalse(pedidoRepository.findById(pedido.id()).isEmpty());
    }
}
