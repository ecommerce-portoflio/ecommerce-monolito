package br.com.ecommerce.model.usuario;

public record DadosAtualizarUsuario(
        String nome,
        String email,
        String senha,
        String telefone
) {
}
