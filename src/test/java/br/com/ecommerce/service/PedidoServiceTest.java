package br.com.ecommerce.service;

import br.com.ecommerce.infra.exception.RegraDeNegocioException;
import br.com.ecommerce.model.carrinho.Carrinho;
import br.com.ecommerce.model.carrinho.ProdutoCarrinho;
import br.com.ecommerce.model.pedido.DadosCadastroPedido;
import br.com.ecommerce.model.pedido.DadosPedido;
import br.com.ecommerce.model.pedido.Pedido;
import br.com.ecommerce.model.pedido.ProdutoPedido;
import br.com.ecommerce.model.pedido.StatusPedido;
import br.com.ecommerce.model.produto.Produto;
import br.com.ecommerce.model.usuario.Usuario;
import br.com.ecommerce.repository.CarrinhoRepository;
import br.com.ecommerce.repository.PedidoRepository;
import br.com.ecommerce.repository.ProdutoCarrinhoRepository;
import br.com.ecommerce.repository.ProdutoPedidoRepository;
import br.com.ecommerce.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private CarrinhoRepository carrinhoRepository;
    @Mock
    private ProdutoPedidoRepository produtoPedidoRepository;
    @Mock
    private ProdutoCarrinhoRepository produtoCarrinhoRepository;

    @InjectMocks
    private PedidoService pedidoService;

    private Usuario comprador;
    private Usuario vendedor;

    @BeforeEach
    void setUp() {
        comprador = criarUsuario(1L);
        vendedor = criarUsuario(2L);
    }

    @Test
    void deveFazerPedidoProdutoUnicoComSucesso() {
        Produto produto = criarProduto(10L, vendedor, 5, BigDecimal.TEN);
        DadosCadastroPedido dados = new DadosCadastroPedido(10L, 2);

        when(produtoRepository.procurarProdutoComVendedor(10L)).thenReturn(Optional.of(produto));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DadosPedido resultado = pedidoService.fazerPedidoProdutoUnico(dados, comprador);

        assertEquals(StatusPedido.AGUARDANDO_PAGAMENTO, resultado.statusPedido());
        assertEquals(3, produto.getQuantidadeEstoque());
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    void deveLancarErroQuandoProdutoNaoEncontradoNoPedidoUnico() {
        when(produtoRepository.procurarProdutoComVendedor(10L)).thenReturn(Optional.empty());

        RegraDeNegocioException ex = assertThrows(
                RegraDeNegocioException.class,
                () -> pedidoService.fazerPedidoProdutoUnico(new DadosCadastroPedido(10L, 1), comprador)
        );

        assertEquals("Produto não encontrado!", ex.getMessage());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void deveLancarErroQuandoEstoqueInsuficienteNoPedidoUnico() {
        Produto produto = criarProduto(10L, vendedor, 1, BigDecimal.TEN);
        when(produtoRepository.procurarProdutoComVendedor(10L)).thenReturn(Optional.of(produto));

        RegraDeNegocioException ex = assertThrows(
                RegraDeNegocioException.class,
                () -> pedidoService.fazerPedidoProdutoUnico(new DadosCadastroPedido(10L, 2), comprador)
        );

        assertEquals("Não há estoque disponível desse produto para essa quantidade!", ex.getMessage());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void deveLancarErroQuandoCompradorCompraProprioProdutoNoPedidoUnico() {
        Produto produto = criarProduto(10L, comprador, 5, BigDecimal.TEN);
        when(produtoRepository.procurarProdutoComVendedor(10L)).thenReturn(Optional.of(produto));

        RegraDeNegocioException ex = assertThrows(
                RegraDeNegocioException.class,
                () -> pedidoService.fazerPedidoProdutoUnico(new DadosCadastroPedido(10L, 1), comprador)
        );

        assertEquals("Você não pode comprar seu próprio produto!", ex.getMessage());
    }

    @Test
    void deveFazerPedidoCarrinhoComSucesso() {
        Carrinho carrinho = new Carrinho(comprador);
        Produto produto = criarProduto(20L, vendedor, 5, new BigDecimal("15.00"));
        carrinho.adicionarProduto(produto, 2);
        Pedido pedidoSalvo = new Pedido(carrinho);

        when(carrinhoRepository.findByUsuario(comprador)).thenReturn(carrinho);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoSalvo);

        DadosPedido resultado = pedidoService.fazerPedidoCarrinho(comprador);

        assertNotNull(resultado);
        assertEquals(StatusPedido.AGUARDANDO_PAGAMENTO, resultado.statusPedido());
        assertEquals(3, produto.getQuantidadeEstoque());
        assertTrue(carrinho.getProdutos().isEmpty());
        assertEquals(0, carrinho.getTotal().compareTo(BigDecimal.ZERO));
    }

    @Test
    void deveLancarErroQuandoCarrinhoVazio() {
        Carrinho carrinho = new Carrinho(comprador);
        when(carrinhoRepository.findByUsuario(comprador)).thenReturn(carrinho);

        RegraDeNegocioException ex = assertThrows(
                RegraDeNegocioException.class,
                () -> pedidoService.fazerPedidoCarrinho(comprador)
        );

        assertEquals("Você não tem produtos no carrinho!", ex.getMessage());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void deveLancarErroQuandoProdutoCarrinhoSemEstoqueSuficiente() {
        Carrinho carrinho = new Carrinho(comprador);
        Produto produto = criarProduto(20L, vendedor, 1, new BigDecimal("15.00"));
        carrinho.adicionarProduto(produto, 2);
        when(carrinhoRepository.findByUsuario(comprador)).thenReturn(carrinho);

        RegraDeNegocioException ex = assertThrows(
                RegraDeNegocioException.class,
                () -> pedidoService.fazerPedidoCarrinho(comprador)
        );

        assertEquals("A quantidade desejada não está disponível para o atual estoque do produto!", ex.getMessage());
    }

    @Test
    void deveFazerPedidoVariosComSucessoEAtualizarCarrinho() {
        Carrinho carrinho = new Carrinho(comprador);
        Produto produto = criarProduto(30L, vendedor, 10, new BigDecimal("20.00"));
        ProdutoCarrinho produtoCarrinho = new ProdutoCarrinho(produto, 2, carrinho);
        carrinho.getProdutos().add(produtoCarrinho);
        carrinho.setTotal(new BigDecimal("40.00"));

        DadosCadastroPedido dados = new DadosCadastroPedido(30L, 2);
        when(carrinhoRepository.findByUsuario(comprador)).thenReturn(carrinho);
        when(produtoRepository.findByIdAndAtivo(30L, true)).thenReturn(Optional.of(produto));
        when(produtoCarrinhoRepository.findByCarrinhoAndProdutoIn(eq(carrinho), org.mockito.ArgumentMatchers.<List<Produto>>any()))
                .thenReturn(List.of(produtoCarrinho));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DadosPedido resultado = pedidoService.fazerPedidoVarios(comprador, List.of(dados));

        assertEquals(StatusPedido.AGUARDANDO_PAGAMENTO, resultado.statusPedido());
        assertEquals(8, produto.getQuantidadeEstoque());
        assertEquals(0, carrinho.getTotal().compareTo(BigDecimal.ZERO));
        assertFalse(carrinho.getProdutos().contains(produtoCarrinho));
    }

    @Test
    void deveLancarErroQuandoProdutoNaoEncontradoNoPedidoVarios() {
        Carrinho carrinho = new Carrinho(comprador);
        when(carrinhoRepository.findByUsuario(comprador)).thenReturn(carrinho);
        when(produtoRepository.findByIdAndAtivo(99L, true)).thenReturn(Optional.empty());

        RegraDeNegocioException ex = assertThrows(
                RegraDeNegocioException.class,
                () -> pedidoService.fazerPedidoVarios(comprador, List.of(new DadosCadastroPedido(99L, 1)))
        );

        assertEquals("Produto de ID 99 não encontrado!", ex.getMessage());
    }

    @Test
    void deveBuscarPedidoComSucessoQuandoUsuarioEhDono() {
        Pedido pedido = criarPedidoComStatus(50L, comprador, vendedor, StatusPedido.AGUARDANDO_PAGAMENTO, 1);
        when(pedidoRepository.findByIdComProdutosEUsuarios(50L)).thenReturn(Optional.of(pedido));

        DadosPedido resultado = pedidoService.buscarPedido(50L, comprador);

        assertEquals(50L, resultado.id());
        assertEquals(comprador.getId(), resultado.compradorId());
    }

    @Test
    void deveLancarErroAoBuscarPedidoDeOutroUsuario() {
        Pedido pedido = criarPedidoComStatus(50L, comprador, vendedor, StatusPedido.AGUARDANDO_PAGAMENTO, 1);
        Usuario outro = criarUsuario(99L);
        when(pedidoRepository.findByIdComProdutosEUsuarios(50L)).thenReturn(Optional.of(pedido));

        RegraDeNegocioException ex = assertThrows(
                RegraDeNegocioException.class,
                () -> pedidoService.buscarPedido(50L, outro)
        );

        assertEquals("Você não tem acesso a esse pedido!", ex.getMessage());
    }

    @Test
    void devePagarPedidoComSucesso() {
        Pedido pedido = criarPedidoComStatus(60L, comprador, vendedor, StatusPedido.AGUARDANDO_PAGAMENTO, 1);
        when(pedidoRepository.findByIdComProdutosEUsuarios(60L)).thenReturn(Optional.of(pedido));

        pedidoService.pagarPedido(60L, comprador);

        assertEquals(StatusPedido.PAGO, pedido.getStatusPedido());
        assertNotNull(pedido.getDataCompra());
    }

    @Test
    void deveLancarErroAoPagarPedidoComStatusInvalido() {
        Pedido pedido = criarPedidoComStatus(60L, comprador, vendedor, StatusPedido.PAGO, 1);
        when(pedidoRepository.findByIdComProdutosEUsuarios(60L)).thenReturn(Optional.of(pedido));

        RegraDeNegocioException ex = assertThrows(
                RegraDeNegocioException.class,
                () -> pedidoService.pagarPedido(60L, comprador)
        );

        assertEquals("Esse pedido já foi pago ou está em andamento!", ex.getMessage());
    }

    @Test
    void deveMarcarPedidoComoEntregueComSucesso() {
        Pedido pedido = criarPedidoComStatus(70L, comprador, vendedor, StatusPedido.PAGO, 1);
        when(pedidoRepository.findByIdComProdutosEUsuarios(70L)).thenReturn(Optional.of(pedido));

        pedidoService.pedidoEntregue(70L);

        assertEquals(StatusPedido.ENTREGUE, pedido.getStatusPedido());
    }

    @Test
    void deveLancarErroAoEntregarPedidoNaoPago() {
        Pedido pedido = criarPedidoComStatus(70L, comprador, vendedor, StatusPedido.AGUARDANDO_PAGAMENTO, 1);
        when(pedidoRepository.findByIdComProdutosEUsuarios(70L)).thenReturn(Optional.of(pedido));

        RegraDeNegocioException ex = assertThrows(
                RegraDeNegocioException.class,
                () -> pedidoService.pedidoEntregue(70L)
        );

        assertEquals("O pedido não foi pago ainda!", ex.getMessage());
    }

    @Test
    void deveCancelarPedidoComSucessoERestaurarEstoque() {
        Pedido pedido = criarPedidoComStatus(80L, comprador, vendedor, StatusPedido.AGUARDANDO_PAGAMENTO, 2);
        Produto produto = pedido.getProdutos().getFirst().getProduto();
        int estoqueAntes = produto.getQuantidadeEstoque();
        when(pedidoRepository.findByIdComProdutosEUsuarios(80L)).thenReturn(Optional.of(pedido));

        pedidoService.cancelarPedido(80L, comprador);

        assertEquals(estoqueAntes + 2, produto.getQuantidadeEstoque());
        verify(pedidoRepository).delete(pedido);
    }

    @Test
    void deveLancarErroAoCancelarPedidoJaPago() {
        Pedido pedido = criarPedidoComStatus(80L, comprador, vendedor, StatusPedido.PAGO, 1);
        when(pedidoRepository.findByIdComProdutosEUsuarios(80L)).thenReturn(Optional.of(pedido));

        RegraDeNegocioException ex = assertThrows(
                RegraDeNegocioException.class,
                () -> pedidoService.cancelarPedido(80L, comprador)
        );

        assertEquals("O pedido já foi pago, não é possível cancelar!", ex.getMessage());
    }

    @Test
    void deveBuscarMeusPedidosComMapeamento() {
        Pedido pedido = criarPedidoComStatus(90L, comprador, vendedor, StatusPedido.AGUARDANDO_PAGAMENTO, 1);
        PageRequest pageable = PageRequest.of(0, 10);
        when(pedidoRepository.findByComprador(comprador, pageable)).thenReturn(new PageImpl<>(List.of(pedido)));

        Page<DadosPedido> resultado = pedidoService.buscarMeusPedidos(comprador, pageable);

        assertEquals(1, resultado.getTotalElements());
        assertEquals(90L, resultado.getContent().getFirst().id());
    }

    private Usuario criarUsuario(Long id) {
        Usuario usuario = new Usuario();
        ReflectionTestUtils.setField(usuario, "id", id);
        return usuario;
    }

    private Produto criarProduto(Long id, Usuario vendedorDoProduto, int estoque, BigDecimal valor) {
        Produto produto = new Produto();
        ReflectionTestUtils.setField(produto, "id", id);
        produto.setVendedor(vendedorDoProduto);
        produto.setQuantidadeEstoque(estoque);
        produto.setValor(valor);
        produto.setAtivo(true);
        return produto;
    }

    private Pedido criarPedidoComStatus(Long idPedido, Usuario compradorDoPedido, Usuario vendedorDoProduto, StatusPedido status, int quantidade) {
        Produto produto = criarProduto(999L, vendedorDoProduto, 10, new BigDecimal("5.00"));
        Pedido pedido = new Pedido(produto, compradorDoPedido, quantidade);
        ReflectionTestUtils.setField(pedido, "id", idPedido);
        pedido.setStatusPedido(status);

        for (ProdutoPedido produtoPedido : pedido.getProdutos()) {
            produtoPedido.setPedido(pedido);
            produtoPedido.setProduto(produto);
        }

        return pedido;
    }
}
