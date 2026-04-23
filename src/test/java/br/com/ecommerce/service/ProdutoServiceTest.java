package br.com.ecommerce.service;

import br.com.ecommerce.infra.exception.RegraDeNegocioException;
import br.com.ecommerce.model.produto.DadosAtualizarProduto;
import br.com.ecommerce.model.produto.DadosCadastroProduto;
import br.com.ecommerce.model.produto.DadosProduto;
import br.com.ecommerce.model.produto.Produto;
import br.com.ecommerce.model.usuario.DadosCadastroUsuario;
import br.com.ecommerce.model.usuario.TipoPessoa;
import br.com.ecommerce.model.usuario.Usuario;
import br.com.ecommerce.repository.ProdutoRepository;
import br.com.ecommerce.repository.UsuarioRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ProdutoService produtoService;

    @Test
    void deveCadastrarProduto() {
        var dto = criaDtoCadastro();
        var usuario = criaUsuario(1L);

        var dtoResposta = produtoService.cadastrar(dto, usuario);

        assertEquals(dto.nome(), dtoResposta.nome());
        assertEquals(dto.descricao(), dtoResposta.descricao());
        assertEquals(dto.valor(), dtoResposta.valor());
        assertEquals(usuario.getId(), dtoResposta.idVendedor());
        verify(produtoRepository).save(any(Produto.class));
    }

    @Test
    void deveBuscarPorId() {
        Produto produto = criaProduto(1L, 1L);
        when(produtoRepository.findByIdAndAtivo(produto.getId(), true)).thenReturn(Optional.of(produto));

        var dtoResposta = produtoService.buscarPorId(produto.getId());

        assertEquals(produto.getNome(), dtoResposta.nome());
        assertEquals(produto.getDescricao(), dtoResposta.descricao());
        assertEquals(produto.getValor(), dtoResposta.valor());
        assertEquals(produto.getVendedor().getId(), dtoResposta.idVendedor());
        assertEquals(produto.getId(), dtoResposta.id());
    }

    @Test
    void deveLancarExcecaoAoNaoEncontrarProduto() {
        when(produtoRepository.findByIdAndAtivo(100L, true)).thenReturn(Optional.empty());

        RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class, () -> produtoService.buscarPorId(100L));
        assertEquals("Produto não encontrado!", ex.getMessage());
    }

    @Test
    void deveBuscarProdutoPorVendedor() {
        Usuario vendedor = criaUsuario(1L);
        Produto produto = criaProduto(vendedor.getId(), 1L);

        var pageable = PageRequest.of(0, 10);
        var produtosPage = new PageImpl<>(List.of(produto));

        when(usuarioRepository.findById(vendedor.getId())).thenReturn(Optional.of(vendedor));
        when(produtoRepository.findAllByVendedorAndAtivo(vendedor, pageable, true)).thenReturn(produtosPage);

        Page<DadosProduto> resposta = produtoService.buscarPorVendedor(vendedor.getId(), pageable);

        assertEquals(1, resposta.getTotalElements());
        assertEquals(produto.getId(), resposta.getContent().getFirst().id());
        assertEquals(produto.getNome(), resposta.getContent().getFirst().nome());
        assertEquals(vendedor.getId(), resposta.getContent().getFirst().idVendedor());
    }

    @Test
    void deveLancarExcecaoQuandoVendedorNaoEncontradoAoBuscarPorVendedor() {
        var pageable = PageRequest.of(0, 10);
        Long idVendedor = 99L;
        when(usuarioRepository.findById(idVendedor)).thenReturn(Optional.empty());

        RegraDeNegocioException ex = assertThrows(
                RegraDeNegocioException.class,
                () -> produtoService.buscarPorVendedor(idVendedor, pageable)
        );

        assertEquals("Vendedor não encontrado!", ex.getMessage());
        verify(produtoRepository, never()).findAllByVendedorAndAtivo(any(), any(), eq(true));
    }

    @Test
    void deveAtualizarProduto() {
        var usuario = criaUsuario(1L);
        var produto = criaProduto(usuario.getId(), 1L);
        var dto = criaDtoAtualizar(produto.getId());
        when(produtoRepository.findByIdAndAtivo(produto.getId(), true)).thenReturn(Optional.of(produto));

        var dtoResposta = produtoService.atualizar(dto, usuario);

        // Verificação se os valores enviados como nulos não alteraram o objeto
        assertEquals(produto.getValor(), dtoResposta.valor());
        assertEquals(produto.getQuantidadeEstoque(), dtoResposta.quantidadeEstoque());
        // Verificação se os valores não nulos alteraram o objeto
        assertEquals(dto.nome(), produto.getNome());
        assertEquals(dto.descricao(), produto.getDescricao());
    }

    @Test
    void deveAtualizarProdutoQuandoUsuarioForAdmin() {
        var usuario = criaUsuario(2L);
        usuario.setRole("ROLE_ADMIN");
        var produto = criaProduto(1L, 1L);
        var dto = criaDtoAtualizar(produto.getId());
        when(produtoRepository.findByIdAndAtivo(produto.getId(), true)).thenReturn(Optional.of(produto));

        var dtoResposta = produtoService.atualizar(dto, usuario);

        // Verificação se os valores enviados como nulos não alteraram o objeto
        assertEquals(produto.getValor(), dtoResposta.valor());
        assertEquals(produto.getQuantidadeEstoque(), dtoResposta.quantidadeEstoque());
        // Verificação se os valores não nulos alteraram o objeto
        assertEquals(dto.nome(), produto.getNome());
        assertEquals(dto.descricao(), produto.getDescricao());
    }

    @Test
    void naoDeveAtualizarProdutoQuandoUsuarioNaoTiverPermissao() {
        var usuario = criaUsuario(2L);
        var produto = criaProduto(1L, 1L);
        var dto = criaDtoAtualizar(produto.getId());
        when(produtoRepository.findByIdAndAtivo(produto.getId(), true)).thenReturn(Optional.of(produto));

        RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class, () -> produtoService.atualizar(dto, usuario));
        assertEquals("Você não tem permissão para atualizar este produto!", ex.getMessage());
    }

    @Test
    void deveDeletarProduto() {
        var usuario = criaUsuario(1L);
        var produto = criaProduto(usuario.getId(), 1L);
        when(produtoRepository.findByIdAndAtivo(produto.getId(), true)).thenReturn(Optional.of(produto));

        produtoService.deletar(produto.getId(), usuario);
        assertFalse(produto.isAtivo());
    }

    @Test
    void deveDeletarProdutoQuandoUsuarioForAdmin() {
        var usuario = criaUsuario(2L);
        usuario.setRole("ROLE_ADMIN");
        var produto = criaProduto(1L, 1L);
        when(produtoRepository.findByIdAndAtivo(produto.getId(), true)).thenReturn(Optional.of(produto));

        produtoService.deletar(produto.getId(), usuario);
        assertFalse(produto.isAtivo());
    }

    @Test
    void naoDeveDeletarProdutoQuandoUsuarioNaoTiverPermissao() {
        var usuario = criaUsuario(2L);
        var produto = criaProduto(1L, 1L);
        when(produtoRepository.findByIdAndAtivo(produto.getId(), true)).thenReturn(Optional.of(produto));

        RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class,
                () -> produtoService.deletar(produto.getId(), usuario));
        assertEquals("Você não tem permissão para alterar este produto!", ex.getMessage());
        assertTrue(produto.isAtivo());
    }

    @Test
    void deveReativarProduto() {
        var usuario = criaUsuario(1L);
        var produto = criaProduto(usuario.getId(), 1L);
        produto.setAtivo(false);
        when(produtoRepository.findById(produto.getId())).thenReturn(Optional.of(produto));

        produtoService.reativar(produto.getId(), usuario);
        assertTrue(produto.isAtivo());
    }

    @Test
    void naoDeveReativarProdutoQuandoUsuarioNaoTiverPermissao() {
        var usuario = criaUsuario(2L);
        var produto = criaProduto(1L, 1L);
        produto.setAtivo(false);
        when(produtoRepository.findById(produto.getId())).thenReturn(Optional.of(produto));

        RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class,
                () -> produtoService.reativar(produto.getId(), usuario));
        assertEquals("Você não tem permissão para alterar este produto!", ex.getMessage());
        assertFalse(produto.isAtivo());
    }

    DadosCadastroProduto criaDtoCadastro() {
        return new DadosCadastroProduto("Nome do Produto", "Descrição do produto", 45, BigDecimal.TEN);
    }

    DadosAtualizarProduto criaDtoAtualizar(Long idProduto) {
        return new DadosAtualizarProduto(idProduto, "Novo nome", "Nova descrição", null, null);
    }

    Usuario criaUsuario(Long id) {
        var dto = new DadosCadastroUsuario("Nome", "email@email.com",
                "Senha***123", "11 900000000", "123.456.789-00", TipoPessoa.PESSOA_FISICA);
        Usuario usuario = new Usuario(dto, dto.senha());
        ReflectionTestUtils.setField(usuario, "id", id);
        return usuario;
    }

    Produto criaProduto(Long idUsuario, Long idProduto) {
        Produto produto = new Produto(criaDtoCadastro(), criaUsuario(idUsuario));
        ReflectionTestUtils.setField(produto, "id", idProduto);
        return produto;
    }
}
