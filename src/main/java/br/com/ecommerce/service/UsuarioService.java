package br.com.ecommerce.service;

import br.com.ecommerce.infra.exception.RegraDeNegocioException;
import br.com.ecommerce.model.usuario.*;
import jakarta.transaction.Transactional;
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

    public DadosUsuario cadastrar(DadosCadastroUsuario dadosCadastro) {
        if (usuarioRepository.existePorDadosUnicos(dadosCadastro.email(),
                dadosCadastro.documento(), dadosCadastro.telefone())) {
            throw new RegraDeNegocioException("Um ou mais dados já foram cadastrados para outro usuário!");
        }

        Usuario usuario = new Usuario(dadosCadastro, passwordEncoder.encode(dadosCadastro.senha()));
        usuarioRepository.save(usuario);
        return new DadosUsuario(usuario);
    }

    public DadosUsuario buscarPorId(Long id) {
        var usuario = usuarioRepository.findById(id).orElseThrow();
        return new DadosUsuario(usuario);
    }

    @Transactional
    public DadosUsuario atualizar(DadosAtualizarUsuario dto, Usuario logado) {
        var usuario = usuarioRepository.findById(logado.getId()).get();
        String novaSenha = (dto.senha() == null || dto.senha().isEmpty()) ? null : passwordEncoder.encode(dto.senha());
        usuario.atualizar(dto, novaSenha);
        return new DadosUsuario(usuario);
    }

    @Transactional
    public void deletar(Long id) {
        var usuario = usuarioRepository.findById(id).orElseThrow();
        usuario.setAtivo(false);
    }
}

