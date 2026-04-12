package br.com.ecommerce.model.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

public record DadosCadastro(
        @NotBlank @Length(min = 3) String nome,
        @NotBlank @Email String email,
        @NotBlank @Length(min = 5) String senha,
        @NotBlank @Pattern(regexp = "^\\d{2} 9\\d{8}$") String telefone,
        @NotBlank String documento,
        @NotNull TipoPessoa tipoPessoa
) {
}
