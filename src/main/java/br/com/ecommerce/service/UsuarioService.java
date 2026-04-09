package br.com.ecommerce.service;

import br.com.ecommerce.model.auth.DadosLogin;
import br.com.ecommerce.model.usuario.DadosCadastro;
import br.com.ecommerce.model.usuario.DadosUsuario;
import br.com.ecommerce.model.usuario.Usuario;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.ecommerce.repository.UsuarioRepository;

@Service
public class UsuarioService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository
                .findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado para o email: " + username));
    }

    public DadosUsuario cadastrar(DadosCadastro dadosCadastro) {
//        TODO: Aumentar validação para: telefone e CPF ou CNPJ
        if (usuarioRepository.existsByEmailIgnoreCase(dadosCadastro.email())) {
//            TODO: Classe personalizada e tratador de erros
            throw new RuntimeException("Usuário já cadastrado com esse email!");
        }
        Usuario usuario = new Usuario(dadosCadastro, passwordEncoder.encode(dadosCadastro.senha()));
        usuarioRepository.save(usuario);
        return new DadosUsuario(usuario);
    }
}

