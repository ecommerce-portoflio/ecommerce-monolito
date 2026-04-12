package br.com.ecommerce.model.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

public record DadosCadastroUsuario(
        @NotBlank(message = "Preencha o nome!")
        @Length(min = 3, message = "O nome deve ter no mínimo 3 letras!")
        @Pattern(regexp = "^\\D+$", message = "Nome não pode conter números!")
        String nome,

        @NotBlank(message = "Preencha o Email!")
        @Email(message = "Formato inválido de Email!")
        String email,

        @NotBlank(message = "Preencha a senha!")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{5,}+$",
                message = "A senha deve conter ao menos: uma letra minúscula, uma letra maiúscula, um número e um símbolo")
        String senha,

        @NotBlank(message = "Preencha o telefone!")
        @Pattern(regexp = "^\\d{2} 9\\d{8}$", message = "Preencha o telefone no formato correto (XX XXXXXXXXX)")
        String telefone,

        @NotBlank(message = "Preencha o documento!")
        @Pattern(regexp = "^(" +
                "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}" + // CPF
                "|" + // OU
                "[A-Za-z0-9]{2}\\.[A-Za-z0-9]{3}\\.[A-Za-z0-9]{3}\\/[A-Za-z0-9]{4}-\\d{2})$", // CNPJ
        message = "Documento em formato inválido!")
        String documento,

        @NotNull(message = "Preencha o tipo de pessoa (PESSOA_FISICA ou PESSOA_JURIDICA)!")
        TipoPessoa tipoPessoa
) {
}
