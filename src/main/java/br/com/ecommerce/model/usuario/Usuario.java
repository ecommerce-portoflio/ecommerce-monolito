package br.com.ecommerce.model.usuario;

import java.util.Collection;
import java.util.Collections;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class Usuario implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    @Column(unique = true)
    private String email;
    private String senha;
    private String telefone;
    @Column(nullable = false)
    private String role;

    @Enumerated(EnumType.STRING)
    private TipoPessoa tipoPessoa;
    private String documento;
    private boolean ativo;

    public Usuario(DadosCadastroUsuario dadosCadastro, String senhaCriptografada) {
        nome = dadosCadastro.nome();
        email = dadosCadastro.email();
        senha = senhaCriptografada;
        telefone = dadosCadastro.telefone();
        role = "ROLE_CLIENTE";
        documento = dadosCadastro.documento();
        tipoPessoa = dadosCadastro.tipoPessoa();
        ativo = true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return ativo;
    }

    public void atualizar(DadosAtualizarUsuario dto, String novaSenha) {
        if (novaSenha != null && !novaSenha.isBlank())
            this.senha = novaSenha;
        if (dto.nome() != null && !dto.nome().isBlank())
            this.nome = dto.nome();
        if (dto.email() != null && !dto.email().isBlank())
            this.email = dto.email();
        if (dto.telefone() != null && !dto.telefone().isBlank())
            this.telefone = dto.telefone();
    }
}
