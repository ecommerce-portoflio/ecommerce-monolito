package br.com.ecommerce.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import br.com.ecommerce.model.carrinho.Carrinho;
import br.com.ecommerce.model.carrinho.ProdutoCarrinho;
import br.com.ecommerce.model.pedido.*;
import br.com.ecommerce.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.ecommerce.infra.exception.RegraDeNegocioException;
import br.com.ecommerce.model.produto.Produto;
import br.com.ecommerce.model.usuario.Usuario;
import jakarta.transaction.Transactional;

@Service
public class PedidoService {
    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final CarrinhoRepository carrinhoRepository;
    private final ProdutoPedidoRepository produtoPedidoRepository;
    private final ProdutoCarrinhoRepository produtoCarrinhoRepository;

    public PedidoService(PedidoRepository pedidoRepository, ProdutoRepository prdoutoRepository, CarrinhoRepository carrinhoRepository, ProdutoPedidoRepository produtoPedidoRepository, ProdutoCarrinhoRepository produtoCarrinhoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = prdoutoRepository;
        this.carrinhoRepository = carrinhoRepository;
        this.produtoPedidoRepository = produtoPedidoRepository;
        this.produtoCarrinhoRepository = produtoCarrinhoRepository;
    }

    @Transactional
    public DadosPedido fazerPedidoProdutoUnico(DadosCadastroPedido dto, Usuario usuarioLogado) {
        Produto produto = produtoRepository.procurarProdutoComVendedor(dto.produtoId())
                .orElseThrow(() -> new RegraDeNegocioException("Produto não encontrado!"));
        validacoesPedido(dto, produto, usuarioLogado);

        Pedido pedido = new Pedido(produto, usuarioLogado, dto.quantidade());
        pedidoRepository.save(pedido);
        produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - dto.quantidade());
        // TODO: Rotina para enviar e-mail aos usuários (comprador e vendedor)
        return new DadosPedido(pedido);
    }

    @Transactional
    public DadosPedido fazerPedidoCarrinho(Usuario usuario) {
        Carrinho carrinho = carrinhoRepository.findByUsuario(usuario);

        validacoesPedidoCarrinho(carrinho);

        Pedido pedido = pedidoRepository.save(new Pedido(carrinho));
        carrinho.getProdutos().forEach(produtoCarrinho -> {
            Produto produto = produtoCarrinho.getProduto();
            produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - produtoCarrinho.getQuantidade());
        });

        carrinho.getProdutos().clear();
        carrinho.setTotal(BigDecimal.ZERO);

        return new DadosPedido(pedido);
    }

    @Transactional
    public DadosPedido fazerPedidoVarios(Usuario usuario, List<DadosCadastroPedido> dto) {
        Pedido pedido = new Pedido(usuario);
        Carrinho carrinho = carrinhoRepository.findByUsuario(usuario);
        List<Produto> listaProdutos = new ArrayList<>();

        dto.forEach(dadosCadastroPedido -> {
            Produto produto = produtoRepository.findByIdAndAtivo(dadosCadastroPedido.produtoId(), true)
                    .orElseThrow(() -> new RegraDeNegocioException("Produto de ID " + dadosCadastroPedido.produtoId() + " não encontrado!"));

            validacoesPedido(dadosCadastroPedido, produto, usuario);

            pedido.acrescentarProduto(produto, dadosCadastroPedido.quantidade());
            produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - dadosCadastroPedido.quantidade());
            listaProdutos.add(produto);
        });

        List<ProdutoCarrinho> produtos = produtoCarrinhoRepository
                .findByCarrinhoAndProdutoIn(carrinho, listaProdutos);
        produtos.forEach(produtoCarrinho -> {
            carrinho.setTotal(carrinho.getTotal()
                            .subtract(produtoCarrinho.getValorUnitario()
                                    .multiply(BigDecimal.valueOf(produtoCarrinho.getQuantidade()))));
            carrinho.getProdutos().remove(produtoCarrinho);
        });

        return new DadosPedido(pedidoRepository.save(pedido));
    }

    public DadosPedido buscarPedido(Long idPedido, Usuario logado) {
        var pedido = buscaPedido(idPedido);

        if (!Objects.equals(pedido.getComprador().getId(), logado.getId()))
            throw new RegraDeNegocioException("Você não tem acesso a esse pedido!");

        return new DadosPedido(pedido);
    }

    @Transactional
    public void pagarPedido(Long idPedido, Usuario usuario) {
        var pedido = buscaPedido(idPedido);
        validacoesPagamento(pedido, usuario);

        pedido.setStatusPedido(StatusPedido.PAGO);
        pedido.setDataCompra(LocalDateTime.now());
        // TODO: Rotina para enviar e-mail ao usuário
    }

    @Transactional
    public void pedidoEntregue(Long idPedido) {
        var pedido = buscaPedido(idPedido);
        validacoesEntrega(pedido);
        pedido.setStatusPedido(StatusPedido.ENTREGUE);
        // TODO: Rotina para enviar email avisando o comprador
        // pedido.getComprador().getEmail()
    }

    public Page<DadosPedido> buscarMeusPedidos(Usuario usuario, Pageable pageable) {
        var pedidos = pedidoRepository.findByComprador(usuario, pageable);
        return pedidos.map(DadosPedido::new);
    }

    public Page<DadosProdutoPedido> buscarMeusPedidosVendidos(Usuario usuario, Pageable pageable) {
        var pedidos = produtoPedidoRepository.buscarProdutosVendidos(usuario, pageable);
        return pedidos.map(DadosProdutoPedido::new);
    }

    @Transactional
    public void cancelarPedido(Long idPedido, Usuario usuario) {
        var pedido = buscaPedido(idPedido);

        validacoesCancelamentoPedido(pedido, usuario);

        // Colocando os produtos novamente em estoque do vendedor
        pedido.getProdutos().forEach(produtoPedido -> {
            var produto = produtoPedido.getProduto();
            produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() + produtoPedido.getQuantidade());
        });

        pedidoRepository.delete(pedido);
    }

    private void validacoesCancelamentoPedido(Pedido pedido, Usuario comprador) {
        if (!Objects.equals(pedido.getComprador().getId(), comprador.getId()))
            throw new RegraDeNegocioException("Você não tem acesso a esse produto!");

        if (pedido.getStatusPedido() == StatusPedido.ENTREGUE || pedido.getStatusPedido() == StatusPedido.PAGO)
            throw new RegraDeNegocioException("O pedido já foi pago, não é possível cancelar!");
    }

    private void validacoesEntrega(Pedido pedido) {
        switch (pedido.getStatusPedido()) {
            case StatusPedido.EM_ANDAMENTO:
                throw new RegraDeNegocioException("O pedido não foi feito ainda!");

            case StatusPedido.AGUARDANDO_PAGAMENTO:
                throw new RegraDeNegocioException("O pedido não foi pago ainda!");

            case StatusPedido.ENTREGUE:
                throw new RegraDeNegocioException("O pedido já foi entregue!");

            default:
                break;
        }

    }

    private void validacoesPagamento(Pedido pedido, Usuario comprador) {
        if (!Objects.equals(pedido.getComprador().getId(), comprador.getId()))
            throw new RegraDeNegocioException("Você não tem acesso a esse pedido!");

        if (pedido.getStatusPedido() != StatusPedido.AGUARDANDO_PAGAMENTO)
            throw new RegraDeNegocioException("Esse pedido já foi pago ou está em andamento!");
    }

    private void validacoesPedido(DadosCadastroPedido dto, Produto produto, Usuario comprador) {
        if (dto.quantidade() > produto.getQuantidadeEstoque())
            throw new RegraDeNegocioException("Não há estoque disponível desse produto para essa quantidade!");
        if (Objects.equals(produto.getVendedor().getId(), comprador.getId()))
            throw new RegraDeNegocioException("Você não pode comprar seu próprio produto!");
    }

    private void validacoesPedidoCarrinho(Carrinho carrinho) {
        if (carrinho.getProdutos().isEmpty())
            throw new RegraDeNegocioException("Você não tem produtos no carrinho!");

//        A validação de estoque do produto é feita
//        novamente pois a quantidade em estoque do produto pode ter mudado desde
//        sua adição ao carrinho do usuário. Uma possibilidade futura é adicionar
//        um status ao produto de que seu estoque está abaixo e invalidar sua compra
        for (var produtoCarrinho : carrinho.getProdutos()) {
            Produto produto = produtoCarrinho.getProduto();
            if (produto.getQuantidadeEstoque() < produtoCarrinho.getQuantidade())
                throw new RegraDeNegocioException("A quantidade desejada não está disponível para o atual estoque do produto!");
        }
    }

    private Pedido buscaPedido(Long idPedido) {
        return pedidoRepository.findByIdComProdutosEUsuarios(idPedido)
                .orElseThrow(() -> new RegraDeNegocioException("Pedido não encontrado!"));
    }
}
