package br.com.ecommerce.service;

import br.com.ecommerce.infra.exception.RegraDeNegocioException;
import br.com.ecommerce.model.avaliacao.Avaliacao;
import br.com.ecommerce.model.avaliacao.DadosCadastroAvaliacao;
import br.com.ecommerce.model.produto.DadosCadastroProduto;
import br.com.ecommerce.model.produto.Produto;
import br.com.ecommerce.model.usuario.DadosCadastroUsuario;
import br.com.ecommerce.model.usuario.TipoPessoa;
import br.com.ecommerce.model.usuario.Usuario;
import br.com.ecommerce.repository.AvaliacaoRepository;
import br.com.ecommerce.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AvaliacaoServiceTest {
    @Mock
    private AvaliacaoRepository avaliacaoRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private AvaliacaoService avaliacaoService;

    @Test
    void deveRegistrarNovaAvaliacao() {
        var usuario = criaUsuario(1L);
        var produto = criaProduto(2L, 2L);
        var dto = criaDtoCadastro(produto.getId(), 3.5);
        when(produtoRepository.findByIdAndAtivo(dto.idProduto(), true)).thenReturn(Optional.of(produto));
        when(avaliacaoRepository.findByAvaliadorAndProduto(usuario, produto)).thenReturn(Optional.empty());

        avaliacaoService.avaliar(usuario, dto);

//        verify(avaliacaoRepository).save(any(Avaliacao.class));
        assertEquals(dto.nota(), produto.getMediaAvaliacoes());
        assertEquals(1, produto.getQuantidadeAvaliacoes());
    }

    @Test
    void deveAtualizarAvaliacao() {
        var usuario = criaUsuario(1L);
        var produto = criaProduto(2L, 2L);
        var dto = criaDtoCadastro(produto.getId(), 3.5);
        var avaliacao = criaAvaliacao(5.0, usuario.getId(), produto.getId());
        produto.avaliar(avaliacao);
        when(produtoRepository.findByIdAndAtivo(dto.idProduto(), true)).thenReturn(Optional.of(produto));
        when(avaliacaoRepository.findByAvaliadorAndProduto(usuario, produto)).thenReturn(Optional.of(avaliacao));

        avaliacaoService.avaliar(usuario, dto);

        verify(avaliacaoRepository, never()).save(any(Avaliacao.class));
        assertEquals(dto.nota(), produto.getMediaAvaliacoes());
        assertEquals(1, produto.getQuantidadeAvaliacoes());
    }

    @Test
    void deveLancarExcecaoAoAvaliarProdutoComIDInvalido() {
        var usuario = criaUsuario(1L);
        var dto = criaDtoCadastro(100L, 3.5);
        when(produtoRepository.findByIdAndAtivo(dto.idProduto(), true)).thenReturn(Optional.empty());

        RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class, () -> avaliacaoService.avaliar(usuario, dto));
        assertEquals("Produto não encontrado!", ex.getMessage());
        verify(avaliacaoRepository, never()).findByAvaliadorAndProduto(any(), any());
    }

    @Test
    void deveRemoverAvaliacao() {
        var usuario = criaUsuario(1L);
        var produto = criaProduto(2L, 2L);
        var avaliacao = criaAvaliacao(5.0, usuario.getId(), produto.getId());
        produto.avaliar(avaliacao);
        when(produtoRepository.findByIdAndAtivo(produto.getId(), true)).thenReturn(Optional.of(produto));
        when(avaliacaoRepository.findByAvaliadorAndProduto(usuario, produto)).thenReturn(Optional.of(avaliacao));

        avaliacaoService.removerAvaliacao(usuario, produto.getId());

        assertEquals(0.0, produto.getMediaAvaliacoes());
        assertEquals(0, produto.getQuantidadeAvaliacoes());
//        verify(avaliacaoRepository).delete(avaliacao);
    }

    @Test
    void deveLancarExcecaoAoDeletarAvaliacaoDeProdutoInvalido() {
        var usuario = criaUsuario(1L);
        when(produtoRepository.findByIdAndAtivo(100L, true)).thenReturn(Optional.empty());

        RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class, () -> avaliacaoService.removerAvaliacao(usuario, 100L));
        assertEquals("Produto não encontrado!", ex.getMessage());
        verify(avaliacaoRepository, never()).findByAvaliadorAndProduto(any(), any());
        verify(avaliacaoRepository, never()).delete(any());
    }

    @Test
    void deveLancarExcecaoAoDeletarAvaliacaoDeUmProdutoNaoAvaliado() {
        var usuario = criaUsuario(1L);
        var produto = criaProduto(2L, 2L);
        when(produtoRepository.findByIdAndAtivo(produto.getId(), true)).thenReturn(Optional.of(produto));
        when(avaliacaoRepository.findByAvaliadorAndProduto(usuario, produto)).thenReturn(Optional.empty());

        RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class, () -> avaliacaoService.removerAvaliacao(usuario, produto.getId()));
        assertEquals("Você não avaliou esse produto!", ex.getMessage());
        verify(avaliacaoRepository, never()).delete(any());
    }

    Avaliacao criaAvaliacao(Double nota, Long idUsuario, Long idProduto) {
        return new Avaliacao(criaUsuario(idUsuario), nota, criaProduto(idUsuario, idProduto));
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

    DadosCadastroAvaliacao criaDtoCadastro(Long idProduto, Double nota) {
        return new DadosCadastroAvaliacao(idProduto, nota);
    }
}
