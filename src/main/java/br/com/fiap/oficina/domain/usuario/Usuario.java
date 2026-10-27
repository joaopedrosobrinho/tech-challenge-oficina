package br.com.fiap.oficina.domain.usuario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Usuario {

    @EqualsAndHashCode.Include
    private Long id;

    private String email;

    private String senha;

    private PerfilUsuario perfil;

    @Builder.Default
    private Boolean ativo = true;
}
