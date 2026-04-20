package br.com.ecommerce.service;

import br.com.ecommerce.infra.exception.RegraDeNegocioException;
import br.com.ecommerce.model.produto.DadosAtualizarProduto;
import br.com.ecommerce.model.produto.DadosCadastroProduto;
import br.com.ecommerce.model.produto.DadosProduto;
import br.com.ecommerce.model.produto.Produto;
import br.com.ecommerce.model.usuario.Usuario;
import br.com.ecommerce.repository.ProdutoRepository;
import br.com.ecommerce.repository.UsuarioRepository;
import jakarta.transaction.Transactional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class ProdutoService {
    private final ProdutoRepository repository;
    private final UsuarioRepository usuarioRepository;

    public ProdutoService(ProdutoRepository repository, UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public DadosProduto cadastrar(DadosCadastroProduto dto, Usuario logado) {
        var produto = new Produto(dto, logado);
        repository.save(produto);
        return new DadosProduto(produto);
    }

    public DadosProduto buscarPorId(Long id) {
        var produto = repository.findByIdAndAtivo(id, true)
            .orElseThrow(() -> new RegraDeNegocioException("Produto não encontrado!"));
        return new DadosProduto(produto);
    }

    public Page<DadosProduto> buscarTodos(Pageable pageable) {
        return repository
                .findAllByAtivo(pageable, true)
                .map(DadosProduto::new);
    }

    public Page<DadosProduto> buscarPorVendedor(Long id, Pageable pageable) {
        var vendedor = usuarioRepository.findById(id)
                .orElseThrow(() -> new RegraDeNegocioException("Vendedor não encontrado!"));
        return repository
                .findAllByVendedorAndAtivo(vendedor, pageable, true)
                .map(DadosProduto::new);
    }

    @Transactional
    public DadosProduto atualizar(DadosAtualizarProduto dto, Usuario logado) {
        var produto = repository.findById(dto.id())
                .orElseThrow(() -> new RegraDeNegocioException("Produto não encontrado!"));
        if (!produto.getVendedor().getId().equals(logado.getId()))
            throw new RegraDeNegocioException("Você não tem permissão para atualizar este produto!");
        produto.atualizar(dto);
        return new DadosProduto(produto);
    }

    @Transactional
    public void deletar(Long id) {
        var produto = repository.findById(id)
                .orElseThrow(() -> new RegraDeNegocioException("Produto não encontrado!"));
        produto.setAtivo(false);
    }
}
