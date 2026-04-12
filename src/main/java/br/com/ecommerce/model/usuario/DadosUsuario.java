package br.com.ecommerce.model.usuario;

public record DadosUsuario(
        String nome,
        String email,
        String telefone,
        Long id
) {
    public DadosUsuario(Usuario usuario) {
        this(usuario.getNome(), usuario.getEmail(), usuario.getTelefone(), usuario.getId());
    }
}
