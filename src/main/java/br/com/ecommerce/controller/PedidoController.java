package br.com.ecommerce.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.ecommerce.model.pedido.DadosCadastroPedido;
import br.com.ecommerce.model.pedido.DadosPedido;
import br.com.ecommerce.model.usuario.Usuario;
import br.com.ecommerce.service.PedidoService;
import jakarta.validation.Valid;

import java.net.URI;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/pedido")
public class PedidoController {
    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<DadosPedido> fazerPedido(@Valid @RequestBody DadosCadastroPedido dto,
            @AuthenticationPrincipal Usuario usuario, UriComponentsBuilder uriBuilder) {
        var pedido = pedidoService.fazerPedidoProdutoUnico(dto, usuario);

        URI uri = uriBuilder
                .path("/pedido/{id}")
                .buildAndExpand(pedido.id())
                .toUri();

        return ResponseEntity.created(uri).body(pedido);
    }

    @PostMapping("/carrinho")
    public ResponseEntity<DadosPedido> fazerPedidoCarrinho(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(pedidoService.fazerPedidoCarrinho(usuario));
    }

//    A diferença entre esse endpoint e o de carrinho é que o usuário pode querer comprar vários,
//    mas não todos os produtos de seu carrinho. Assim, ele compra alguns e os mesmos são removidos
//    do carrinho.
    @PostMapping("/varios")
    public ResponseEntity<DadosPedido> fazerPedidoVarios(@AuthenticationPrincipal Usuario usuario,
                                                         @RequestBody List<DadosCadastroPedido> dto) {
        return ResponseEntity.ok(pedidoService.fazerPedidoVarios(usuario, dto));
    }

    // Esse endpoint simula apenas o recebimento, não contém nenhuma API real de
    // banco fazendo um pagamento
    @PostMapping("/pagar/{idPedido}")
    public ResponseEntity<String> pagarPedido(@AuthenticationPrincipal Usuario usuario, @PathVariable Long idPedido) {
        pedidoService.pagarPedido(idPedido, usuario);
        return ResponseEntity.ok("Pagamento concluído!");
    }

    // Esse endpoint seria chamado pela transportadora para avisar a entrega do
    // pedido
    @PostMapping("/entregar/{idPedido}")
    public ResponseEntity<String> pedidoEntregue(@PathVariable Long idPedido) {
        pedidoService.pedidoEntregue(idPedido);

        return ResponseEntity.ok("Pedido registrado como entregue no sistema!");
    }

    @GetMapping("/{idPedido}")
    public ResponseEntity<DadosPedido> buscarPedido(@PathVariable Long idPedido,
            @AuthenticationPrincipal Usuario logado) {
        var pedido = pedidoService.buscarPedido(idPedido, logado);
        return ResponseEntity.ok(pedido);
    }

    @GetMapping("/todos")
    public Page<?> verMeusPedidos(@AuthenticationPrincipal Usuario usuario,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return pedidoService.buscarMeusPedidos(usuario, pageable);
    }

    @GetMapping("/todos/vendidos")
    public Page<?> verMeusPedidosVendidos(@AuthenticationPrincipal Usuario usuario,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return pedidoService.buscarMeusPedidosVendidos(usuario, pageable);
    }

    @DeleteMapping("/{idPedido}")
    public ResponseEntity<String> cancelarPedido(@PathVariable Long idPedido,
            @AuthenticationPrincipal Usuario usuario) {
        pedidoService.cancelarPedido(idPedido, usuario);
        return ResponseEntity.ok("Pedido cancelado com sucesso!");
    }
}
