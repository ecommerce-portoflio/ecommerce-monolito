package br.com.ecommerce.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.ecommerce.infra.exception.RegraDeNegocioException;
import br.com.ecommerce.model.usuario.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import br.com.ecommerce.repository.UsuarioRepository;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Captor
    ArgumentCaptor<Usuario> captor;

    @Test
    void deveCadastrarUsuario(){
        DadosCadastroUsuario dto = criaDtoCadastro();

        usuarioService.cadastrar(dto);
        verify(usuarioRepository).save(captor.capture());
        Usuario usuarioSalvo = captor.getValue();

        assertEquals(dto.nome(), usuarioSalvo.getNome());
        assertEquals(dto.email(), usuarioSalvo.getEmail());
        verify(passwordEncoder).encode(dto.senha());
    }

    @Test
    void naoDeveCadastrarUsuarioComDadosJaCadastrados() {
        DadosCadastroUsuario dto = criaDtoCadastro();
        when(usuarioRepository.existePorDadosUnicos(dto.email(), dto.documento(), dto.telefone())).thenReturn(true);

        RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class, () -> usuarioService.cadastrar(dto));
        assertEquals("Um ou mais dados já foram cadastrados para outro usuário!", ex.getMessage());
    }

    @Test
    void deveBuscarUsuarioPorId() {
        DadosCadastroUsuario dto = criaDtoCadastro();
        Usuario usuario = criaUsuario(1L, dto);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        DadosUsuario dtoResposta = usuarioService.buscarPorId(1L);

        assertEquals(usuario.getNome(), dtoResposta.nome());
        assertEquals(usuario.getEmail(), dtoResposta.email());
        assertEquals(usuario.getTelefone(), dtoResposta.telefone());
        assertEquals(usuario.getId(), dtoResposta.id());
    }

    @Test
    void deveAtualizarUsuario() {
        DadosCadastroUsuario dto = criaDtoCadastro();
        Usuario usuario = criaUsuario(1L, dto);
        DadosAtualizarUsuario dtoAtualizar = new DadosAtualizarUsuario("Novo Nome", "novoemail@email.com",
                "novaSenha123", "11 922222222");
        when(usuarioRepository.findById(any(Long.class))).thenReturn(Optional.of(usuario));

        DadosUsuario dtoResultado = usuarioService.atualizar(dtoAtualizar, usuario);

        assertEquals(dtoAtualizar.nome(), dtoResultado.nome());
        assertEquals(dtoAtualizar.email(), dtoResultado.email());
        assertEquals(dtoAtualizar.telefone(), dtoResultado.telefone());
        verify(passwordEncoder).encode(dtoAtualizar.senha());
    }

    @Test
    void deveDeletarUsuario() {
        DadosCadastroUsuario dto = criaDtoCadastro();
        Usuario usuario = criaUsuario(1L, dto);
        when(usuarioRepository.findById(any(Long.class))).thenReturn(Optional.of(usuario));

        usuarioService.deletar(1L);

        assertFalse(usuario.isAtivo());
    }

    private Usuario criaUsuario(Long id, DadosCadastroUsuario dto) {
        Usuario usuario = new Usuario(dto, passwordEncoder.encode(dto.senha()));
        ReflectionTestUtils.setField(usuario, "id", id);
        return usuario;
    }

    private DadosCadastroUsuario criaDtoCadastro() {
        return new DadosCadastroUsuario("Nome", "email@email.com",
        "Senha***123", "11 900000000", "123.456.789-00", TipoPessoa.PESSOA_FISICA);
    }
}
