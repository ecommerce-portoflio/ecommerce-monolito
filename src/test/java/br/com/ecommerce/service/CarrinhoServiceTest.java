package br.com.ecommerce.service;

import br.com.ecommerce.infra.exception.RegraDeNegocioException;
import br.com.ecommerce.model.carrinho.DadosCadastroProdutoCarrinho;
import br.com.ecommerce.model.carrinho.ProdutoCarrinho;
import br.com.ecommerce.model.produto.DadosCadastroProduto;
import br.com.ecommerce.model.produto.Produto;
import br.com.ecommerce.model.usuario.DadosCadastroUsuario;
import br.com.ecommerce.model.usuario.TipoPessoa;
import br.com.ecommerce.model.usuario.Usuario;
import br.com.ecommerce.repository.CarrinhoRepository;
import br.com.ecommerce.repository.ProdutoCarrinhoRepository;
import br.com.ecommerce.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CarrinhoServiceTest {
    @Mock
    private CarrinhoRepository carrinhoRepository;
    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private ProdutoCarrinhoRepository produtoCarrinhoRepository;
    @InjectMocks
    private CarrinhoService carrinhoService;

    @Test
    void deveAdicionarProdutoAoCarrinho() {
        Usuario usuario = criaUsuario(1L);
        Produto produto = criaProduto(2L, 2L);
        DadosCadastroProdutoCarrinho dto = new DadosCadastroProdutoCarrinho(produto.getId(), 2);
        when(carrinhoRepository.findByUsuario(usuario)).thenReturn(usuario.getCarrinho());
        when(produtoRepository.findByIdAndAtivo(produto.getId(), true)).thenReturn(Optional.of(produto));

        carrinhoService.adicionarProduto(dto, usuario);

        assertEquals(0, usuario.getCarrinho().getTotal().compareTo(new BigDecimal("20")));
        assertEquals(1, usuario.getCarrinho().getProdutos().size());
    }

    @Test
    void naoDeveAdicionarProprioProdutoAoCarrinho() {
        Usuario usuario = criaUsuario(1L);
        Produto produto = criaProduto(usuario.getId(), 2L);
        DadosCadastroProdutoCarrinho dto = new DadosCadastroProdutoCarrinho(produto.getId(), 2);
        when(carrinhoRepository.findByUsuario(usuario)).thenReturn(usuario.getCarrinho());
        when(produtoRepository.findByIdAndAtivo(dto.idProduto(), true)).thenReturn(Optional.of(produto));

        RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class,
                () -> carrinhoService.adicionarProduto(dto, usuario));
        assertEquals("Você não pode adicionar seu próprio produto no carrinho!", ex.getMessage());
        assertEquals(0, usuario.getCarrinho().getTotal().compareTo(BigDecimal.ZERO));
        assertEquals(0, usuario.getCarrinho().getProdutos().size());
    }

    @Test
    void naoDeveAdicionarProdutoComQuantidadeMaiorQueADisponivelEmEstoque() {
        Usuario usuario = criaUsuario(1L);
        Produto produto = criaProduto(2L, 2L);
        DadosCadastroProdutoCarrinho dto = new DadosCadastroProdutoCarrinho(produto.getId(), 50);
        when(carrinhoRepository.findByUsuario(usuario)).thenReturn(usuario.getCarrinho());
        when(produtoRepository.findByIdAndAtivo(dto.idProduto(), true)).thenReturn(Optional.of(produto));

        RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class,
                () -> carrinhoService.adicionarProduto(dto, usuario));
        assertEquals("A quantidade desejada não está disponível para o atual estoque do produto!", ex.getMessage());
        assertEquals(0, usuario.getCarrinho().getTotal().compareTo(BigDecimal.ZERO));
        assertEquals(0, usuario.getCarrinho().getProdutos().size());
    }

    @Test
    void deveLancarExcecaoAoTentarAdicionarProdutoInexistenteAoCarrinho() {
        Usuario usuario = criaUsuario(1L);
        DadosCadastroProdutoCarrinho dto = new DadosCadastroProdutoCarrinho(100L, 50);
        when(carrinhoRepository.findByUsuario(usuario)).thenReturn(usuario.getCarrinho());
        when(produtoRepository.findByIdAndAtivo(dto.idProduto(), true)).thenReturn(Optional.empty());

        RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class,
                () -> carrinhoService.adicionarProduto(dto, usuario));
        assertEquals("Produto não encontrado!", ex.getMessage());
        assertEquals(0, usuario.getCarrinho().getTotal().compareTo(BigDecimal.ZERO));
        assertEquals(0, usuario.getCarrinho().getProdutos().size());
    }

    @Test
    void deveBuscarCarrinho() {
        Usuario usuario = criaUsuario(1L);
        usuario.getCarrinho().adicionarProduto(criaProduto(2L, 2L), 2);
        usuario.getCarrinho().adicionarProduto(criaProduto(2L, 3L), 2);
        when(carrinhoRepository.findByUsuario(usuario)).thenReturn(usuario.getCarrinho());

        var dtoResultado = carrinhoService.buscarCarrinho(usuario);

        assertEquals(2, dtoResultado.produtos().size());
        assertEquals(0, dtoResultado.total().compareTo(BigDecimal.valueOf(40)));
    }

    @Test
    void deveRemoverProdutoDoCarrinho() {
        Usuario usuario = criaUsuario(1L);
        Produto produto = criaProduto(2L, 2L);
        ProdutoCarrinho produtoCarrinho = new ProdutoCarrinho(produto, 2, usuario.getCarrinho());

//        Manipulação manual de ProdutoCarrinho pois no método é feito pela JPA
        usuario.getCarrinho().getProdutos().add(produtoCarrinho);
        usuario.getCarrinho().setTotal(produtoCarrinho.getValorUnitario()
                .multiply(BigDecimal.valueOf(produtoCarrinho.getQuantidade())));

        when(carrinhoRepository.findByUsuario(usuario)).thenReturn(usuario.getCarrinho());
        when(produtoRepository.findByIdAndAtivo(2L, true)).thenReturn(Optional.of(produto));
        when(produtoCarrinhoRepository.findByProdutoAndCarrinho(produto, usuario.getCarrinho())).thenReturn(Optional.of(produtoCarrinho));

        carrinhoService.removerProduto(2L, usuario);

        assertEquals(0, usuario.getCarrinho().getProdutos().size());
        assertEquals(0, usuario.getCarrinho().getTotal().compareTo(BigDecimal.ZERO));
    }

    @Test
    void deveLancarExcecaoAoTentarRemoverProdutoNaoAdicionadoAoCarrinho() {
        Usuario usuario = criaUsuario(1L);
        Produto produto = criaProduto(2L, 2L);
        Produto produtoForaCarrinho = criaProduto(2L, 3L);
        ProdutoCarrinho produtoCarrinho = new ProdutoCarrinho(produto, 2, usuario.getCarrinho());

//        Manipulação manual de ProdutoCarrinho pois no método é feito pela JPA
        usuario.getCarrinho().getProdutos().add(produtoCarrinho);
        usuario.getCarrinho().setTotal(produtoCarrinho.getValorUnitario()
                .multiply(BigDecimal.valueOf(produtoCarrinho.getQuantidade())));

        when(carrinhoRepository.findByUsuario(usuario)).thenReturn(usuario.getCarrinho());
        when(produtoRepository.findByIdAndAtivo(3L, true)).thenReturn(Optional.of(produtoForaCarrinho));
        when(produtoCarrinhoRepository.findByProdutoAndCarrinho(produtoForaCarrinho, usuario.getCarrinho())).thenReturn(Optional.empty());

        RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class, () -> carrinhoService.removerProduto(3L, usuario));
        assertEquals("Produto informado não se encontra no seu carrinho!", ex.getMessage());
        assertEquals(1, usuario.getCarrinho().getProdutos().size());
        assertEquals(0, usuario.getCarrinho().getTotal().compareTo(BigDecimal.valueOf(20)));
    }

    @Test
    void deveLancarExcecaoAoTentarRemoverProdutoInexistenteDoCarrinho() {
        Usuario usuario = criaUsuario(1L);
        Produto produto = criaProduto(2L, 2L);
        ProdutoCarrinho produtoCarrinho = new ProdutoCarrinho(produto, 2, usuario.getCarrinho());

//        Manipulação manual de ProdutoCarrinho pois no método é feito pela JPA
        usuario.getCarrinho().getProdutos().add(produtoCarrinho);
        usuario.getCarrinho().setTotal(produtoCarrinho.getValorUnitario()
                .multiply(BigDecimal.valueOf(produtoCarrinho.getQuantidade())));

        when(carrinhoRepository.findByUsuario(usuario)).thenReturn(usuario.getCarrinho());
        when(produtoRepository.findByIdAndAtivo(300L, true)).thenReturn(Optional.empty());

        RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class, () -> carrinhoService.removerProduto(300L, usuario));
        assertEquals("Produto não encontrado!", ex.getMessage());
        assertEquals(1, usuario.getCarrinho().getProdutos().size());
        assertEquals(0, usuario.getCarrinho().getTotal().compareTo(BigDecimal.valueOf(20)));
        verify(produtoCarrinhoRepository, never()).findByProdutoAndCarrinho(any(), any());
    }

    @Test
    void deveEsvaziarCarrinho() {
        Usuario usuario = criaUsuario(1L);
        Produto produto = criaProduto(2L, 2L);
        Produto produto2 = criaProduto(2L, 3L);
        ProdutoCarrinho produtoCarrinho = new ProdutoCarrinho(produto, 2, usuario.getCarrinho());
        ProdutoCarrinho produtoCarrinho2 = new ProdutoCarrinho(produto2, 2, usuario.getCarrinho());

        //        Manipulação manual de ProdutoCarrinho pois no método é feito pela JPA
        usuario.getCarrinho().getProdutos().add(produtoCarrinho);
        usuario.getCarrinho().setTotal(produtoCarrinho.getValorUnitario()
                .multiply(BigDecimal.valueOf(produtoCarrinho.getQuantidade())));
        usuario.getCarrinho().getProdutos().add(produtoCarrinho2);
        usuario.getCarrinho().setTotal(produtoCarrinho2.getValorUnitario()
                .multiply(BigDecimal.valueOf(produtoCarrinho2.getQuantidade())));

        when(carrinhoRepository.findByUsuario(usuario)).thenReturn(usuario.getCarrinho());

        carrinhoService.esvaziarCarrinho(usuario);

//        verify(produtoCarrinhoRepository).deleteAllByCarrinho(usuario.getCarrinho());
        assertEquals(0, usuario.getCarrinho().getTotal().compareTo(BigDecimal.ZERO));
        assertEquals(0, usuario.getCarrinho().getProdutos().size());
    }

    Usuario criaUsuario(Long id) {
        var dto = new DadosCadastroUsuario("Nome", "email@email.com",
                "Senha***123", "11 900000000", "123.456.789-00", TipoPessoa.PESSOA_FISICA);
        Usuario usuario = new Usuario(dto, dto.senha());
        ReflectionTestUtils.setField(usuario, "id", id);
        return usuario;
    }

    Produto criaProduto(Long idUsuario, Long idProduto) {
        Produto produto = new Produto(new DadosCadastroProduto("Nome do Produto", "Descrição do produto",
                45, BigDecimal.TEN), criaUsuario(idUsuario));
        ReflectionTestUtils.setField(produto, "id", idProduto);
        return produto;
    }

}
