package br.com.ecommerce.model.usuario;

public record DadosCadastro(
        String nome,
        String email,
        String senha,
        String telefone,
        String cpf,
        String cnpj
) {
}
