package br.com.ecommerce.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.ecommerce.infra.exception.RegraDeNegocioException;
import br.com.ecommerce.model.pedido.DadosCadastroPedido;
import br.com.ecommerce.model.pedido.DadosPedido;
import br.com.ecommerce.model.pedido.Pedido;
import br.com.ecommerce.model.pedido.StatusPedido;
import br.com.ecommerce.model.produto.Produto;
import br.com.ecommerce.model.usuario.Usuario;
import br.com.ecommerce.repository.PedidoRepository;
import br.com.ecommerce.repository.ProdutoRepository;
import jakarta.transaction.Transactional;

@Service
public class PedidoService {
    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;

    public PedidoService(PedidoRepository pedidoRepository, ProdutoRepository prdoutoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = prdoutoRepository;
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

    public DadosPedido buscarPedido(Long idPedido, Usuario logado) {
        var pedido = buscaPedido(idPedido);

        if (pedido.getComprador().getId() != logado.getId() && pedido.getVendedor().getId() != logado.getId())
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
        return pedidos.map(pedido -> new DadosPedido(pedido));
    }

    public Page<DadosPedido> buscarMeusPedidosVendidos(Usuario usuario, Pageable pageable) {
        var pedidos = pedidoRepository.findByVendedor(usuario, pageable);
        return pedidos.map(pedido -> new DadosPedido(pedido));
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
        if (pedido.getComprador().getId() != comprador.getId())
            throw new RegraDeNegocioException("Você não tem acesso a esse produto!");
        
        if (pedido.getStatusPedido() == StatusPedido.ENTREGUE || pedido.getStatusPedido() == StatusPedido.PAGO )
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
        if (pedido.getComprador().getId() != comprador.getId())
            throw new RegraDeNegocioException("Você não tem acesso a esse pedido!");

        if (pedido.getStatusPedido() != StatusPedido.AGUARDANDO_PAGAMENTO)
            throw new RegraDeNegocioException("Esse pedido já foi pago ou está em andamento!");
    }

    private void validacoesPedido(DadosCadastroPedido dto, Produto produto, Usuario comprador) {
        if (dto.quantidade() > produto.getQuantidadeEstoque())
            throw new RegraDeNegocioException("Não há estoque disponível desse produto para essa quantidade!");
        if (produto.getVendedor().getId() == comprador.getId())
            throw new RegraDeNegocioException("Você não pode comprar seu próprio produto!");
    }

    private Pedido buscaPedido(Long idPedido) {
        return pedidoRepository.findByIdComProdutosEUsuarios(idPedido)
                .orElseThrow(() -> new RegraDeNegocioException("Pedido não encontrado!"));
    }
}
